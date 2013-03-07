package org.graphbi.sql2neo.wrapper;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.graphbi.sql2neo.transformer.Transformer;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;

public class NeoWrapper implements Wrapper {
    private static Logger log = Logger.getLogger(NeoWrapper.class);

    private static enum RelTypes implements RelationshipType {
	INSTANCE
    }

    private static final String REFERENCE_KEY = "reference";

    private final GraphDatabaseService graphdb;
    private Index<Node> nodeIndex;
    private Index<Node> referenceIndex;

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

    /**
     * Creates a Node based on the given properties. If the reference node for
     * the given type doesn't exist, it will be created and indexed.
     * 
     * @param properties
     *            Properties for the new node.
     */
    public void createNode(Map<String, Object> properties) {
	String type = (String) properties.get(Transformer.TYPE_KEY);
	Node refNode = getReferenceNode(type);
	// create node
	Node node = graphdb.createNode();
	for (Map.Entry<String, Object> e : properties.entrySet()) {
	    node.setProperty(e.getKey(), checkSupportedType(e.getValue()));
	}
	nodeIndex.add(node, Transformer.ID_KEY,
		properties.get(Transformer.ID_KEY));

	// create edge between refNode and new node
	refNode.createRelationshipTo(node, RelTypes.INSTANCE);
	
	log.debug(String.format("Created Neo4j node: %s", node));
    }

    public void createRelationship(String sourceID, String targetID,
	    String relType, Map<String, Object> properties) {
	// TODO Auto-generated method stub

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

    private Object checkSupportedType(Object o) {
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
