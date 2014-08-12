package org.graphbi.rdb2graph.util.graph.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.graphbi.rdb2graph.util.config.Constants;
import org.graphbi.rdb2graph.util.config.NodeSuperClass;
import org.graphbi.rdb2graph.util.graph.ReadWriteGraph;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.tooling.GlobalGraphOperations;

/**
 * A wrapper for Neo4j.
 * 
 * @author Martin Junghanns
 * 
 */
public class Neo4jGraph implements ReadWriteGraph {
	private static Logger log = Logger.getLogger(Neo4jGraph.class);

	/*
	 * Used to link instance nodes with reference / type nodes.
	 * 
	 * @author Martin Junghanns
	 */
	private static enum RelTypes implements RelationshipType {
		INSTANCE
	}

	/*
	 * Used in the reference node index as key in the key/value pair for a node.
	 */
	private static final String REFERENCE_KEY = "reference";
	/*
	 * Used in the nodes index as key in the key/value pair for a node.
	 */
	private static final String NODE_KEY = "rdb2graph_id";

	private final GraphDatabaseService graphdb;

	private final Index<Node> nodeIndex;
	private final Index<Node> referenceIndex;

	private Transaction tx;

	private Map<String, Node> referenceNodes;

	public Neo4jGraph(GraphDatabaseService graphdb) {
		this.graphdb = graphdb;

		try (Transaction tx = graphdb.beginTx()) {
			nodeIndex = graphdb.index().forNodes("Instances");
			referenceIndex = graphdb.index().forNodes("References");
			tx.success();
		}
		referenceNodes = new HashMap<String, Node>();
	}

	public GraphDatabaseService getGraphDB() {
		return graphdb;
	}

	public void beginTransaction() {
		if (tx == null) {
			tx = graphdb.beginTx();
		}
	}

	public void successTransaction() {
		if (tx != null) {
			tx.success();
		}
	}

	public void finishTransaction() {
		if (tx != null) {
			tx.close();
			tx = null;
		}
	}

	public void rollbackTransaction() {
		if (tx != null) {
			tx.failure();
		}
	}

	@Override
	public Long createNode(final Map<String, Object> properties) {
		return createNode(properties, true, true);
	}

	@Override
	public Long createNode(final Map<String, Object> properties,
			boolean useIndex) {
		return createNode(properties, useIndex, true);
	}

	@Override
	public Long createNode(final Map<String, Object> properties,
			boolean useIndex, boolean useReferenceNode) {
		String type = (String) properties.get(Constants.CLASS_KEY);
		Node refNode = null;
		if (useReferenceNode) {
			refNode = getReferenceNode(type);
		}
		// create node
		Node node = graphdb.createNode();
		for (Map.Entry<String, Object> e : properties.entrySet()) {
			node.setProperty(e.getKey(), getSupportedType(e.getValue()));
		}
		if (useIndex) {
			nodeIndex.add(node, NODE_KEY, properties.get(Constants.ID_KEY));
		}

		// create edge between refNode and new node
		if (refNode != null && useReferenceNode) {
			refNode.createRelationshipTo(node, RelTypes.INSTANCE);
		}

		log.debug(String.format("Created Neo4j node: %s", node));
		return node.getId();
	}

	@Override
	public Long createRelationship(final String sourceID,
			final String targetID, final String relType) {
		return createRelationship(sourceID, targetID, relType, null);
	}

	@Override
	public Long createRelationship(final String sourceID,
			final String targetID, final String relType,
			final Map<String, Object> properties) {
		Node source = nodeIndex.get(NODE_KEY, sourceID).getSingle();
		Node target = nodeIndex.get(NODE_KEY, targetID).getSingle();
		return createRelationship(source, target, relType, null);
	}

	@Override
	public Long createRelationship(final Long sourceID, final Long targetID,
			final String relType) {
		return createRelationship(sourceID, targetID, relType, null);
	}

	@Override
	public Long createRelationship(final Long sourceID, final Long targetID,
			final String relType, final Map<String, Object> properties) {
		Node source = null, target = null;
		if (sourceID != null && targetID != null) {
			try {
				source = graphdb.getNodeById(sourceID);
				target = graphdb.getNodeById(targetID);
			} catch (NotFoundException ex) {
				log.error(ex);
			}
		}
		return createRelationship(source, target, relType, null);
	}

	@Override
	public String getName() {
		return "Neo4j";
	}

	@Override
	public String getNodeClass(Long nodeId) {
		Node v = graphdb.getNodeById(nodeId);
		if (v.hasProperty(Constants.CLASS_KEY)) {
			return (String) v.getProperty(Constants.CLASS_KEY);
		}
		return null;
	}

	@Override
	public Set<Long> getNodesBySuperClass(
			Map<String, NodeSuperClass> typeClassMap, NodeSuperClass nodeClass) {
		Set<Long> nodes = new HashSet<Long>();
		String nodeType;
		try (Transaction tx = graphdb.beginTx()) {
			for (Node n : GlobalGraphOperations.at(graphdb).getAllNodes()) {
				nodeType = (String) n.getProperty(Constants.CLASS_KEY, null);
				if (nodeType != null && typeClassMap.containsKey(nodeType)
						&& typeClassMap.get(nodeType).equals(nodeClass)) {
					nodes.add(n.getId());
				}
			}
		}
		return nodes;
	}

	@Override
	public Set<Long> getIncidentEdges(Long nodeId) {
		return getIncidentEdges(nodeId, false);
	}

	@Override
	public Set<Long> getIncidentEdges(Long nodeId, boolean skipInstance) {
		Set<Long> edgeIds = new HashSet<Long>();
		for (Relationship e : graphdb.getNodeById(nodeId).getRelationships(
				Direction.BOTH)) {
			if (!e.isType(RelTypes.INSTANCE)) {
				edgeIds.add(e.getId());
			}
		}
		return edgeIds;
	}

	@Override
	public Long[] getIncidentNodes(Long edgeId) {
		if (edgeId == null) {
			throw new IllegalArgumentException("edgeId must not be null.");
		}
		Relationship e = graphdb.getRelationshipById(edgeId);
		return new Long[] { e.getStartNode().getId(), e.getEndNode().getId() };
	}

	@Override
	public Set<Long> getAdjacentNodes(Long nodeId) {
		Set<Long> nodes = new HashSet<Long>();
		Node v = graphdb.getNodeById(nodeId);
		for (Relationship e : v.getRelationships(Direction.BOTH)) {
			nodes.add(e.getEndNode().getId());
		}
		return nodes;
	}

	@Override
	public Map<String, Object> getNodeProperties(Long nodeId) {
		if (nodeId == null) {
			throw new IllegalArgumentException("nodeId must not be null");
		}
		try (Transaction tx = graphdb.beginTx()) {
			return getProperties(graphdb.getNodeById(nodeId));
		} catch (NotFoundException ex) {
			log.error(ex);
		}
		return null;
	}

	@Override
	public Map<String, Object> getEdgeProperties(Long edgeId) {
		if (edgeId == null) {
			throw new IllegalArgumentException("edgeId must not be null");
		}
		try {
			return getProperties(graphdb.getRelationshipById(edgeId));
		} catch (NotFoundException ex) {
			log.error(ex);
		}
		return null;
	}

	@Override
	public String getEdgeType(Long edgeId) {
		if (edgeId == null) {
			throw new IllegalArgumentException("edgeId must not be null");
		}
		try {
			return graphdb.getRelationshipById(edgeId).getType().name()
					.toString();
		} catch (NotFoundException ex) {
			log.error(ex);
		}
		return null;
	}

	/**
	 * Returns a Map of the properties of a node / relationship.
	 * 
	 * @param propContainer
	 *            Node or Relationship
	 * 
	 * @return Node's / Relationship's properties.
	 */
	private Map<String, Object> getProperties(PropertyContainer propContainer) {
		Map<String, Object> res = new HashMap<String, Object>();
		for (String propKey : propContainer.getPropertyKeys()) {
			res.put(propKey, propContainer.getProperty(propKey));
		}
		return res;
	}

	/**
	 * Creates a relationship between two given nodes (if they exist).
	 * 
	 * @param source
	 *            Relationship's source node.
	 * @param target
	 *            Relationship's target node.
	 * @param relType
	 *            Relationship's type.
	 * @param properties
	 *            Relationship's properties as key-value-pairs.
	 * 
	 * @return The system specific relationship id or null if one of the
	 *         incident nodes was null.
	 */
	private Long createRelationship(final Node source, final Node target,
			final String relType, final Map<String, Object> properties) {
		if (source != null && target != null) {
			log.debug(String.format("Connecting %s and %s", source, target));
			Relationship rel = source.createRelationshipTo(target,
					DynamicRelationshipType.withName(relType));
			if (properties != null) {
				for (Entry<String, Object> p : properties.entrySet()) {
					rel.setProperty(p.getKey(), getSupportedType(p.getValue()));
				}
			}
			return rel.getId();
		}
		return null;
	}

	private Node getReferenceNode(String type) {
		return (referenceNodes.containsKey(type)) ? referenceNodes.get(type)
				: createReferenceNode(type);
	}

	private Node createReferenceNode(String type) {
		Node refNode = graphdb.createNode();
		refNode.setProperty(REFERENCE_KEY, type);
		referenceNodes.put(type, refNode);
		referenceIndex.add(refNode, REFERENCE_KEY, type);
		return refNode;
	}

	private Object getSupportedType(Object o) {
		if (o instanceof Boolean || o instanceof Byte || o instanceof Short
				|| o instanceof Integer || o instanceof Long
				|| o instanceof Float || o instanceof Double
				|| o instanceof Character || o instanceof String) {
			return o;
		} else {
			return o.toString();
		}
	}
}
