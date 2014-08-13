package org.graphbi.rdb2graph.util.graph.impl.analyzer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.graphbi.rdb2graph.analysis.documentgraph.DocGraph;
import org.graphbi.rdb2graph.analysis.documentgraph.analyzer.DocGraphFilterFunction;
import org.graphbi.rdb2graph.analysis.documentgraph.analyzer.ResourceInvolvementExtractor;
import org.graphbi.rdb2graph.util.config.Constants;
import org.graphbi.rdb2graph.util.graph.ReadOnlyGraph;
import org.graphbi.rdb2graph.util.graph.impl.Neo4jGraph;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.Traversal;

public class NeoResourceInvolvementExtractor implements
		ResourceInvolvementExtractor {
	private static Logger log = Logger
			.getLogger(NeoResourceInvolvementExtractor.class);

	private final GraphDatabaseService graphDB;

	private PathFinder<Path> finder;

	public NeoResourceInvolvementExtractor(GraphDatabaseService graphDB) {
		this.graphDB = graphDB;
	}

	public NeoResourceInvolvementExtractor(ReadOnlyGraph graph) {
		if (!(graph instanceof Neo4jGraph)) {
			throw new IllegalArgumentException(
					"graph must be instance of Neo4jGraph");
		}

		this.graphDB = ((Neo4jGraph) graph).getGraphDB();
	}

	@Override
	public Map<String, Set<Long>> extract(List<DocGraph> docGraphs,
			DocGraphFilterFunction filterFunc, Set<String> resourceClasses,
			Set<String> documentClasses) {
		// pattern-document graph associations R_D
		Map<String, Set<Long>> associations = new HashMap<String, Set<Long>>();

		// init path finder
		initFinder();

		// G_D(filtered) <- G_D.where({G_d|filter(G_D)})
		Set<DocGraph> filteredDocGraphs = new HashSet<DocGraph>();
		if (filterFunc == null) {
			filteredDocGraphs.addAll(docGraphs);
		} else {
			for (DocGraph docGraph : docGraphs) {
				if (filterFunc.filter(docGraph)) {
					filteredDocGraphs.add(docGraph);
				}
			}
		}

		Set<Node> relevantResources = null;
		Set<Node> relevantDocuments = null;
		String r = null;
		Node v_p = null;
		boolean validPath = true;
		int pCnt = 0;
		log.info(String.format("Starting analysis of %d document graphs",
				filteredDocGraphs.size()));
		for (DocGraph docGraph : filteredDocGraphs) {
			log.info(String.format("Analyzing DocGraph[%d] (%d, %d)",
					docGraph.getId(), docGraph.getNodeCount(),
					docGraph.getEdgeCount()));
			pCnt = 0;
			// V_R <- G_d.V.where({v|tau(mu(v)) in C_R})
			relevantResources = filterNodesByClass(docGraph.getNodes(),
					resourceClasses);
			// V_R <- G_d.V.where({v|tau(mu(v)) in C_D})
			relevantDocuments = filterNodesByClass(docGraph.getNodes(),
					documentClasses);

			preProcessGraph(docGraph, resourceClasses);
			try (Transaction tx = graphDB.beginTx()) {
				for (Node v_r : relevantResources) {
					for (Node v_d : relevantDocuments) {
						// P <- G_d.paths(v_R,v_D)
						for (Path p : finder.findAllPaths(v_r, v_d)) {
							validPath = true;
							r = String.format("%s",
									v_r.getProperty(Constants.ID_KEY));
							v_p = v_r;
							for (Relationship e : p.relationships()) {
								if (e.getStartNode().getId() == v_p.getId()) {
									if (getNodeSuperClass(e.getEndNode())
											.equals(Constants.NODE_SUPER_CLASS_RESOURCE_VALUE)) {
										validPath = false;
										break;
									}
									r = String.format("%s-%s->%s", r, e
											.getType().name(), getNodeClass(e
											.getEndNode()));
									v_p = e.getEndNode();
								} else {
									if (getNodeSuperClass(e.getStartNode())
											.equals(Constants.NODE_SUPER_CLASS_RESOURCE_VALUE)) {
										validPath = false;
										break;
									}
									r = String.format("%s<-%s-%s", r, e
											.getType().name(), getNodeClass(e
											.getStartNode()));
									v_p = e.getStartNode();
								}
							}
							// R_D.add(<r, G_d>)
							if (validPath) {
								associations = addPatternDocGraphAssociation(
										associations, r, docGraph);
								pCnt++;
							}
						}
					}

				}
				tx.success();
			}
			log.info(String.format("%d patterns in DocGraph[%d]", pCnt,
					docGraph.getId()));
		}
		return associations;
	}

	/**
	 * Removes all nodes with non-valid resource classes from the graph.
	 * 
	 * @param docGraph
	 * @param validResourceClasses
	 */
	private void preProcessGraph(DocGraph docGraph,
			Set<String> validResourceClasses) {
		log.info(String.format("Preprocessing DocGraph[%d]", docGraph.getId()));
		Node n;
		int cnt = 0;
		try (Transaction tx = graphDB.beginTx()) {
			for (Long nodeId : docGraph.getNodes()) {
				n = graphDB.getNodeById(nodeId);
				// node's class is resource class and it's not in the valid set
				if (n.hasProperty(Constants.NODE_SUPER_CLASS_KEY)
						&& n.getProperty(Constants.NODE_SUPER_CLASS_KEY)
								.equals(Constants.NODE_SUPER_CLASS_RESOURCE_VALUE)
						&& !validResourceClasses.contains(getNodeClass(n))) {
					try (Transaction innerTx = graphDB.beginTx()) {

						log.info("Deleting " + n);
						for (Relationship e : n.getRelationships()) {
							e.delete();
						}
						n.delete();
						innerTx.success();
						cnt++;
					}
				}
			}
			tx.success();
		}

		log.info(String.format(
				"Preprocessing removed %d nodes from DocGraph[%d]", cnt,
				docGraph.getId()));
	}

	private Set<Node> filterNodesByClass(Set<Long> nodes,
			Set<String> validNodeClasses) {

		Node n = null;
		Set<Node> result = new HashSet<Node>();
		try (Transaction tx = graphDB.beginTx()) {
			for (Long nodeId : nodes) {
				try {
					n = graphDB.getNodeById(nodeId);
					// valid node class?
					if (n.hasProperty(Constants.CLASS_KEY)
							&& validNodeClasses.contains((String) n
									.getProperty(Constants.CLASS_KEY))) {
						result.add(n);
					}
				} catch (NotFoundException e) {
					log.error("Node not found in database: " + nodeId);
					continue;
				}
			}
			tx.success();
		}
		return result;
	}

	private String getNodeClass(Node n) {
		if (n.hasProperty(Constants.CLASS_KEY)) {
			return (String) n.getProperty(Constants.CLASS_KEY);
		} else {
			return "";
		}
	}

	private String getNodeSuperClass(Node n) {
		if (n.hasProperty(Constants.NODE_SUPER_CLASS_KEY)) {
			return (String) n.getProperty(Constants.NODE_SUPER_CLASS_KEY);
		} else {
			return "";
		}
	}

	private void initFinder() {
		this.finder = GraphAlgoFactory.allSimplePaths(
				Traversal.expanderForAllTypes(), 7);
	}

	private Map<String, Set<Long>> addPatternDocGraphAssociation(
			Map<String, Set<Long>> associations, final String pattern,
			final DocGraph docGraph) {
		if (associations.containsKey(pattern)) {
			associations.get(pattern).add(docGraph.getId());
		} else {
			associations.put(pattern, new HashSet<Long>() {
				private static final long serialVersionUID = 1L;
				{
					add(docGraph.getId());
				}
			});
		}
		return associations;
	}
}
