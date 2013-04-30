package org.graphbi.rdb2graph.test;

import java.util.List;

import junit.framework.Assert;

import org.graphbi.rdb2graph.analysis.operationgraph.OperationGraph;
import org.graphbi.rdb2graph.analysis.operationgraph.OperationGraphExtractor;
import org.graphbi.rdb2graph.analysis.operationgraph.OperationGraphDuplicator;
import org.graphbi.rdb2graph.util.graph.impl.Neo4jGraph;
import org.junit.Test;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.neo4j.tooling.GlobalGraphOperations;

public class OperationGraphDuplicatorTest extends GraphBITest {

    @Test
    public void testOperationGraphMovingResult() {
	Neo4jGraph fromGraph = new Neo4jGraph(graphDb);
	GraphDatabaseService toGraphDB = new TestGraphDatabaseFactory()
		.newImpermanentDatabaseBuilder().newGraphDatabase();
	Neo4jGraph toGraph = new Neo4jGraph(toGraphDB);

	List<OperationGraph> opGraphs = new OperationGraphExtractor(fromGraph,
		nodeClassSuperClassMap).extract();

	OperationGraphDuplicator mover = new OperationGraphDuplicator(cfg,
		fromGraph, toGraph);
	mover.duplicate(opGraphs);

	// get node and edge count of the new graph
	int n = IteratorUtil.count(GlobalGraphOperations.at(toGraphDB)
		.getAllNodes());
	int m = IteratorUtil.count(GlobalGraphOperations.at(toGraphDB)
		.getAllRelationships());

	// 12 document nodes
	// + 2 * 4 resource nodes
	// + 6 reference nodes
	// + 1 root node
	Assert.assertEquals(12 + (2 * 4) + 6 + 1, n);
	// 9 edges in SalesGraph 1
	// + 13 edges in SalesGraph 2
	// + 16 (+4 duplicated resource nodes) edges for INSTANCE relationship
	Assert.assertEquals(9 + 13 + 16 + 4, m);

	Assert.assertTrue(toGraphDB.index().existsForNodes("Instances"));

	Index<Node> nodeIndex = toGraphDB.index().forNodes("Instances");

	final String RDB2GRAPH_ID = "rdb2graph_id";

	// documents nodes are unique in the whole graph
	Assert.assertEquals(1, IteratorUtil.count(nodeIndex.get(RDB2GRAPH_ID,
		KEY_Q001).iterator()));
	Assert.assertEquals(1, IteratorUtil.count(nodeIndex.get(RDB2GRAPH_ID,
		KEY_Q002).iterator()));
	Assert.assertEquals(1, IteratorUtil.count(nodeIndex.get(RDB2GRAPH_ID,
		KEY_S001).iterator()));
	Assert.assertEquals(1, IteratorUtil.count(nodeIndex.get(RDB2GRAPH_ID,
		KEY_S002).iterator()));
	Assert.assertEquals(1, IteratorUtil.count(nodeIndex.get(RDB2GRAPH_ID,
		KEY_I001).iterator()));
	Assert.assertEquals(1, IteratorUtil.count(nodeIndex.get(RDB2GRAPH_ID,
		KEY_I002).iterator()));
	Assert.assertEquals(1, IteratorUtil.count(nodeIndex.get(RDB2GRAPH_ID,
		KEY_I003).iterator()));
	Assert.assertEquals(1, IteratorUtil.count(nodeIndex.get(RDB2GRAPH_ID,
		KEY_INVITEM001).iterator()));
	Assert.assertEquals(1, IteratorUtil.count(nodeIndex.get(RDB2GRAPH_ID,
		KEY_INVITEM002).iterator()));
	Assert.assertEquals(1, IteratorUtil.count(nodeIndex.get(RDB2GRAPH_ID,
		KEY_INVITEM003).iterator()));
	Assert.assertEquals(1, IteratorUtil.count(nodeIndex.get(RDB2GRAPH_ID,
		KEY_INVITEM004).iterator()));
	Assert.assertEquals(1, IteratorUtil.count(nodeIndex.get(RDB2GRAPH_ID,
		KEY_INVITEM005).iterator()));

	// resource nodes are duplicated for each opgraph they act in
	Assert.assertEquals(2, IteratorUtil.count(nodeIndex.get(RDB2GRAPH_ID,
		KEY_TOM).iterator()));
	Assert.assertEquals(2, IteratorUtil.count(nodeIndex.get(RDB2GRAPH_ID,
		KEY_JOHN).iterator()));
	Assert.assertEquals(2, IteratorUtil.count(nodeIndex.get(RDB2GRAPH_ID,
		KEY_EXPENSE).iterator()));
	Assert.assertEquals(2, IteratorUtil.count(nodeIndex.get(RDB2GRAPH_ID,
		KEY_REVENUE).iterator()));

	// check relationships
	// Q001
	Node node = nodeIndex.get(RDB2GRAPH_ID, KEY_Q001).getSingle();
	Assert.assertEquals(3, IteratorUtil.count(node.getRelationships()));
	Assert.assertEquals(1, IteratorUtil.count(node.getRelationships(
		Direction.INCOMING, RelTypes.INSTANCE)));
	Assert.assertEquals(1, IteratorUtil.count(node.getRelationships(
		Direction.INCOMING, RelTypes.basedOn)));
	Assert.assertEquals(1, IteratorUtil.count(node.getRelationships(
		Direction.OUTGOING, RelTypes.sentBy)));
	// Q002
	node = nodeIndex.get(RDB2GRAPH_ID, KEY_Q002).getSingle();
	Assert.assertEquals(3, IteratorUtil.count(node.getRelationships()));
	Assert.assertEquals(1, IteratorUtil.count(node.getRelationships(
		Direction.INCOMING, RelTypes.INSTANCE)));
	Assert.assertEquals(1, IteratorUtil.count(node.getRelationships(
		Direction.INCOMING, RelTypes.basedOn)));
	Assert.assertEquals(1, IteratorUtil.count(node.getRelationships(
		Direction.OUTGOING, RelTypes.sentBy)));
	// S001
	node = nodeIndex.get(RDB2GRAPH_ID, KEY_S001).getSingle();
	Assert.assertEquals(4, IteratorUtil.count(node.getRelationships()));
	Assert.assertEquals(1, IteratorUtil.count(node.getRelationships(
		Direction.INCOMING, RelTypes.INSTANCE)));
	Assert.assertEquals(1, IteratorUtil.count(node.getRelationships(
		Direction.INCOMING, RelTypes.bills)));
	Assert.assertEquals(1, IteratorUtil.count(node.getRelationships(
		Direction.OUTGOING, RelTypes.processedBy)));
	Assert.assertEquals(1, IteratorUtil.count(node.getRelationships(
		Direction.OUTGOING, RelTypes.basedOn)));
	// S002
	node = nodeIndex.get(RDB2GRAPH_ID, KEY_S002).getSingle();
	Assert.assertEquals(5, IteratorUtil.count(node.getRelationships()));
	Assert.assertEquals(1, IteratorUtil.count(node.getRelationships(
		Direction.INCOMING, RelTypes.INSTANCE)));
	Assert.assertEquals(2, IteratorUtil.count(node.getRelationships(
		Direction.INCOMING, RelTypes.bills)));
	Assert.assertEquals(1, IteratorUtil.count(node.getRelationships(
		Direction.OUTGOING, RelTypes.processedBy)));
	Assert.assertEquals(1, IteratorUtil.count(node.getRelationships(
		Direction.OUTGOING, RelTypes.basedOn)));
	// I001
	node = nodeIndex.get(RDB2GRAPH_ID, KEY_I001).getSingle();
	Assert.assertEquals(5, IteratorUtil.count(node.getRelationships()));
	Assert.assertEquals(1, IteratorUtil.count(node.getRelationships(
		Direction.INCOMING, RelTypes.INSTANCE)));
	Assert.assertEquals(1, IteratorUtil.count(node.getRelationships(
		Direction.OUTGOING, RelTypes.bills)));
	Assert.assertEquals(1, IteratorUtil.count(node.getRelationships(
		Direction.OUTGOING, RelTypes.createdBy)));
	Assert.assertEquals(2, IteratorUtil.count(node.getRelationships(
		Direction.INCOMING, RelTypes.causedBy)));
	// I002
	node = nodeIndex.get(RDB2GRAPH_ID, KEY_I002).getSingle();
	Assert.assertEquals(5, IteratorUtil.count(node.getRelationships()));
	Assert.assertEquals(1, IteratorUtil.count(node.getRelationships(
		Direction.INCOMING, RelTypes.INSTANCE)));
	Assert.assertEquals(1, IteratorUtil.count(node.getRelationships(
		Direction.OUTGOING, RelTypes.bills)));
	Assert.assertEquals(1, IteratorUtil.count(node.getRelationships(
		Direction.OUTGOING, RelTypes.createdBy)));
	Assert.assertEquals(2, IteratorUtil.count(node.getRelationships(
		Direction.INCOMING, RelTypes.causedBy)));
	// I003
	node = nodeIndex.get(RDB2GRAPH_ID, KEY_I003).getSingle();
	Assert.assertEquals(4, IteratorUtil.count(node.getRelationships()));
	Assert.assertEquals(1, IteratorUtil.count(node.getRelationships(
		Direction.INCOMING, RelTypes.INSTANCE)));
	Assert.assertEquals(1, IteratorUtil.count(node.getRelationships(
		Direction.OUTGOING, RelTypes.bills)));
	Assert.assertEquals(1, IteratorUtil.count(node.getRelationships(
		Direction.OUTGOING, RelTypes.createdBy)));
	Assert.assertEquals(1, IteratorUtil.count(node.getRelationships(
		Direction.INCOMING, RelTypes.causedBy)));
	// INVITEM001
	node = nodeIndex.get(RDB2GRAPH_ID, KEY_INVITEM001).getSingle();
	Assert.assertEquals(3, IteratorUtil.count(node.getRelationships()));
	Assert.assertEquals(1, IteratorUtil.count(node.getRelationships(
		Direction.INCOMING, RelTypes.INSTANCE)));
	Assert.assertEquals(1, IteratorUtil.count(node.getRelationships(
		Direction.OUTGOING, RelTypes.causedBy)));
	Assert.assertEquals(1, IteratorUtil.count(node.getRelationships(
		Direction.OUTGOING, RelTypes.postedOn)));
	// INVITEM002
	node = nodeIndex.get(RDB2GRAPH_ID, KEY_INVITEM002).getSingle();
	Assert.assertEquals(3, IteratorUtil.count(node.getRelationships()));
	Assert.assertEquals(1, IteratorUtil.count(node.getRelationships(
		Direction.INCOMING, RelTypes.INSTANCE)));
	Assert.assertEquals(1, IteratorUtil.count(node.getRelationships(
		Direction.OUTGOING, RelTypes.causedBy)));
	Assert.assertEquals(1, IteratorUtil.count(node.getRelationships(
		Direction.OUTGOING, RelTypes.postedOn)));
	// INVITEM003
	node = nodeIndex.get(RDB2GRAPH_ID, KEY_INVITEM003).getSingle();
	Assert.assertEquals(3, IteratorUtil.count(node.getRelationships()));
	Assert.assertEquals(1, IteratorUtil.count(node.getRelationships(
		Direction.INCOMING, RelTypes.INSTANCE)));
	Assert.assertEquals(1, IteratorUtil.count(node.getRelationships(
		Direction.OUTGOING, RelTypes.causedBy)));
	Assert.assertEquals(1, IteratorUtil.count(node.getRelationships(
		Direction.OUTGOING, RelTypes.postedOn)));
	// INVITEM004
	node = nodeIndex.get(RDB2GRAPH_ID, KEY_INVITEM004).getSingle();
	Assert.assertEquals(3, IteratorUtil.count(node.getRelationships()));
	Assert.assertEquals(1, IteratorUtil.count(node.getRelationships(
		Direction.INCOMING, RelTypes.INSTANCE)));
	Assert.assertEquals(1, IteratorUtil.count(node.getRelationships(
		Direction.OUTGOING, RelTypes.causedBy)));
	Assert.assertEquals(1, IteratorUtil.count(node.getRelationships(
		Direction.OUTGOING, RelTypes.postedOn)));
	// INVITEM005
	node = nodeIndex.get(RDB2GRAPH_ID, KEY_INVITEM005).getSingle();
	Assert.assertEquals(3, IteratorUtil.count(node.getRelationships()));
	Assert.assertEquals(1, IteratorUtil.count(node.getRelationships(
		Direction.INCOMING, RelTypes.INSTANCE)));
	Assert.assertEquals(1, IteratorUtil.count(node.getRelationships(
		Direction.OUTGOING, RelTypes.causedBy)));
	Assert.assertEquals(1, IteratorUtil.count(node.getRelationships(
		Direction.OUTGOING, RelTypes.postedOn)));
    }
}
