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
	Iterable<Path> paths = null;
	String r = null;
	Node v_p = null;
	for (DocGraph docGraph : filteredDocGraphs) {
	    // V_R <- G_d.V.where({v|tau(mu(v)) in C_R})
	    relevantResources = filterNodesByClass(docGraph.getNodes(),
		    resourceClasses);
	    // V_R <- G_d.V.where({v|tau(mu(v)) in C_D})
	    relevantDocuments = filterNodesByClass(docGraph.getNodes(),
		    documentClasses);
	    for (Node v_r : relevantResources) {
		for (Node v_d : relevantDocuments) {
		    // P <- G_d.paths(v_R,v_D)
		    paths = finder.findAllPaths(v_r, v_d);
		    for (Path p : paths) {
			r = String.format("%s",
				v_r.getProperty(Constants.ID_KEY));
			v_p = v_r;
			for (Relationship e : p.relationships()) {
			    if (e.getStartNode().getId() == v_p.getId()) {
				r = String.format("%s-%s->%s", r, e.getType(),
					getNodeClass(e.getEndNode()));
				v_p = e.getEndNode();
			    } else {
				r = String.format("%s<-%s-%s", r, e.getType(),
					getNodeClass(e.getStartNode()));
				v_p = e.getStartNode();
			    }
			}
			// R_D.add(<r, G_d>)
			associations = addPatternDocGraphAssociation(
				associations, r, docGraph);
		    }
		}
	    }
	}
	return associations;
    }

    private Set<Node> filterNodesByClass(Set<Long> nodes,
	    Set<String> validNodeClasses) {

	Node n = null;
	Set<Node> result = new HashSet<Node>();
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
	return result;
    }

    private String getNodeClass(Node n) {
	if (n.hasProperty(Constants.CLASS_KEY)) {
	    return (String) n.getProperty(Constants.CLASS_KEY);
	} else {
	    return "";
	}
    }

    private void initFinder() {
	this.finder = GraphAlgoFactory.allSimplePaths(
		Traversal.expanderForAllTypes(), 10);
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
