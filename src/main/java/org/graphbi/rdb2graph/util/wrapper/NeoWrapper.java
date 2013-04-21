package org.graphbi.rdb2graph.util.wrapper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.graphbi.rdb2graph.analysis.wrapper.GraphAnalysisWrapper;
import org.graphbi.rdb2graph.transformation.wrapper.GraphTransformationWrapper;
import org.graphbi.rdb2graph.util.config.Constants;
import org.graphbi.rdb2graph.util.config.NodeSuperClass;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.tooling.GlobalGraphOperations;

public class NeoWrapper implements GraphTransformationWrapper,
	GraphAnalysisWrapper {
    private static Logger log = Logger.getLogger(NeoWrapper.class);

    /*
     * Used to the link instance nodes with reference / type nodes.
     * 
     * @author s1ck
     */
    private static enum RelTypes implements RelationshipType {
	INSTANCE
    }

    /*
     * Used in the reference node index to retrieve content.
     */
    private static final String REFERENCE_KEY = "reference";

    private final GraphDatabaseService graphdb;

    private final Index<Node> nodeIndex;
    private final Index<Node> referenceIndex;

    private Transaction tx;

    private Map<String, Node> referenceNodes;

    public NeoWrapper(GraphDatabaseService graphdb) {
	this.graphdb = graphdb;
	nodeIndex = graphdb.index().forNodes("nodes");
	referenceIndex = graphdb.index().forNodes("references");

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
	    tx.finish();
	    tx = null;
	}
    }

    public void rollbackTransaction() {
	if (tx != null) {
	    tx.failure();
	}
    }

    /**
     * Creates a Node based on the given properties. If the reference node for
     * the given type doesn't exist, it will be created and indexed.
     * 
     * @param properties
     *            Properties for the new node.
     */
    public boolean createNode(final Map<String, Object> properties) {
	String type = (String) properties.get(Constants.CLASS_KEY);
	Node refNode = getReferenceNode(type);
	// create node
	Node node = graphdb.createNode();
	for (Map.Entry<String, Object> e : properties.entrySet()) {
	    node.setProperty(e.getKey(), getSupportedType(e.getValue()));
	}
	nodeIndex.add(node, Constants.ID_KEY, properties.get(Constants.ID_KEY));

	// create edge between refNode and new node
	refNode.createRelationshipTo(node, RelTypes.INSTANCE);

	log.debug(String.format("Created Neo4j node: %s", node));
	return node != null;
    }

    public boolean createRelationship(final String sourceID,
	    final String targetID, final String relType) {
	return createRelationship(sourceID, targetID, relType, null);
    }

    /**
     * Creates a link between to given nodes if those node exist.
     */
    public boolean createRelationship(final String sourceID,
	    final String targetID, final String relType,
	    final Map<String, Object> properties) {
	Node source = nodeIndex.get(Constants.ID_KEY, sourceID).getSingle();
	Node target = nodeIndex.get(Constants.ID_KEY, targetID).getSingle();
	if (source != null && target != null) {
	    log.debug(String.format("Connecting %s and %s", source, target));
	    Relationship rel = source.createRelationshipTo(target,
		    DynamicRelationshipType.withName(relType));
	    if (properties != null) {
		for (Entry<String, Object> p : properties.entrySet()) {
		    rel.setProperty(p.getKey(), getSupportedType(p.getValue()));
		}
	    }
	    return true;
	} else {
	    if (source == null) {
		log.error(String
			.format("Error during creation of relationship with type '%s': Source node '%s' not found in index",
				relType, sourceID));
	    }
	    if (target == null) {
		log.error(String
			.format("Error during creation of relationship with type '%s': Target node '%s' not found in index",
				relType, targetID));
	    }
	}
	return false;
    }

    public void printNodes(String type) {
	Node refNode = referenceIndex.get(REFERENCE_KEY, type).getSingle();
	if (refNode != null) {
	    for (Relationship edge : refNode.getRelationships(
		    Direction.OUTGOING, RelTypes.INSTANCE)) {
		System.out.println("\t" + edge.getEndNode());
	    }
	}
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
	for (Node n : GlobalGraphOperations.at(graphdb).getAllNodes()) {
	    nodeType = (String) n.getProperty(Constants.CLASS_KEY, null);
	    if (nodeType != null && typeClassMap.containsKey(nodeType)
		    && typeClassMap.get(nodeType).equals(nodeClass)) {
		nodes.add(n.getId());
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
    public Set<Long> getIncidentNodes(Long edgeId) {
	Set<Long> nodes = new HashSet<Long>();
	Relationship e = graphdb.getRelationshipById(edgeId);
	nodes.add(e.getStartNode().getId());
	nodes.add(e.getEndNode().getId());
	return nodes;
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
}
