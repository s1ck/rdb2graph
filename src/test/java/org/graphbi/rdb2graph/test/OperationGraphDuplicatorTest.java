package org.graphbi.rdb2graph.test;

import java.util.List;

import junit.framework.Assert;

import org.graphbi.rdb2graph.analysis.operationgraph.OperationGraph;
import org.graphbi.rdb2graph.analysis.operationgraph.OperationGraphExtractor;
import org.graphbi.rdb2graph.analysis.operationgraph.OperationGraphDuplicator;
import org.graphbi.rdb2graph.util.graph.impl.Neo4jGraph;
import org.junit.Test;
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

	OperationGraphDuplicator mover = new OperationGraphDuplicator(
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
	
	// documents nodes are unique in the whole graph
	Assert.assertEquals(1, IteratorUtil.count(nodeIndex.get("rdb2graph_id",
		KEY_Q001).iterator()));
	Assert.assertEquals(1, IteratorUtil.count(nodeIndex.get("rdb2graph_id",
		KEY_Q002).iterator()));
	Assert.assertEquals(1, IteratorUtil.count(nodeIndex.get("rdb2graph_id",
		KEY_S001).iterator()));
	Assert.assertEquals(1, IteratorUtil.count(nodeIndex.get("rdb2graph_id",
		KEY_S002).iterator()));
	Assert.assertEquals(1, IteratorUtil.count(nodeIndex.get("rdb2graph_id",
		KEY_I001).iterator()));
	Assert.assertEquals(1, IteratorUtil.count(nodeIndex.get("rdb2graph_id",
		KEY_I002).iterator()));
	Assert.assertEquals(1, IteratorUtil.count(nodeIndex.get("rdb2graph_id",
		KEY_I003).iterator()));
	Assert.assertEquals(1, IteratorUtil.count(nodeIndex.get("rdb2graph_id",
		KEY_INVITEM001).iterator()));
	Assert.assertEquals(1, IteratorUtil.count(nodeIndex.get("rdb2graph_id",
		KEY_INVITEM002).iterator()));
	Assert.assertEquals(1, IteratorUtil.count(nodeIndex.get("rdb2graph_id",
		KEY_INVITEM003).iterator()));
	Assert.assertEquals(1, IteratorUtil.count(nodeIndex.get("rdb2graph_id",
		KEY_INVITEM004).iterator()));
	Assert.assertEquals(1, IteratorUtil.count(nodeIndex.get("rdb2graph_id",
		KEY_INVITEM005).iterator()));
	
	// resource nodes are duplicated for each opgraph they act in
	Assert.assertEquals(2, IteratorUtil.count(nodeIndex.get("rdb2graph_id",
		KEY_TOM).iterator()));
	Assert.assertEquals(2, IteratorUtil.count(nodeIndex.get("rdb2graph_id",
		KEY_JOHN).iterator()));
	Assert.assertEquals(2, IteratorUtil.count(nodeIndex.get("rdb2graph_id",
		KEY_EXPENSE).iterator()));
	Assert.assertEquals(2, IteratorUtil.count(nodeIndex.get("rdb2graph_id",
		KEY_REVENUE).iterator()));
    }
}
