package org.graphbi.rdb2graph.test;

import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.graphbi.rdb2graph.analysis.operationgraph.OperationGraph;
import org.graphbi.rdb2graph.analysis.operationgraph.OperationGraphExtractor;
import org.graphbi.rdb2graph.util.graph.impl.Neo4jGraph;
import org.junit.Test;

public class OperationGraphAnalysisTest extends GraphBITest {

    @Test
    public void testOperationGraphExtractionOpGraphCount() {
	OperationGraphExtractor extractor = new OperationGraphExtractor(
		new Neo4jGraph(graphDb), nodeClassSuperClassMap);
	List<OperationGraph> opGraphs = extractor.extract();

	Assert.assertEquals(2, opGraphs.size());
    }

    @Test
    public void testOperationGraphExtractionOpGraphContent() {
	OperationGraphExtractor extractor = new OperationGraphExtractor(
		new Neo4jGraph(graphDb), nodeClassSuperClassMap);
	List<OperationGraph> opGraphs = extractor.extract();

	Assert.assertEquals(2, opGraphs.size());
	Set<Long> nodes;
	Set<Long> edges;
	for (OperationGraph opGraph : opGraphs) {
	    nodes = opGraph.getNodes();
	    edges = opGraph.getEdges();
	    // check if it's Sales OpGraph 1
	    if (nodes.contains(nodeIdMap.get(KEY_Q001))) {
		Assert.assertEquals(9, nodes.size());
		Assert.assertEquals(9, edges.size());
		// documents
		Assert.assertEquals(true,
			nodes.contains(nodeIdMap.get(KEY_Q001)));
		Assert.assertEquals(true,
			nodes.contains(nodeIdMap.get(KEY_S001)));
		Assert.assertEquals(true,
			nodes.contains(nodeIdMap.get(KEY_I001)));
		Assert.assertEquals(true,
			nodes.contains(nodeIdMap.get(KEY_INVITEM001)));
		Assert.assertEquals(true,
			nodes.contains(nodeIdMap.get(KEY_INVITEM002)));
		// ressources
		Assert.assertEquals(true,
			nodes.contains(nodeIdMap.get(KEY_TOM)));
		Assert.assertEquals(true,
			nodes.contains(nodeIdMap.get(KEY_JOHN)));
		Assert.assertEquals(true,
			nodes.contains(nodeIdMap.get(KEY_EXPENSE)));
		Assert.assertEquals(true,
			nodes.contains(nodeIdMap.get(KEY_REVENUE)));
	    } else {
		// Sales OpGraph2
		// documents
		Assert.assertEquals(true,
			nodes.contains(nodeIdMap.get(KEY_Q002)));
		Assert.assertEquals(true,
			nodes.contains(nodeIdMap.get(KEY_S002)));
		Assert.assertEquals(true,
			nodes.contains(nodeIdMap.get(KEY_I002)));
		Assert.assertEquals(true,
			nodes.contains(nodeIdMap.get(KEY_I003)));
		Assert.assertEquals(true,
			nodes.contains(nodeIdMap.get(KEY_INVITEM003)));
		Assert.assertEquals(true,
			nodes.contains(nodeIdMap.get(KEY_INVITEM004)));
		Assert.assertEquals(true,
			nodes.contains(nodeIdMap.get(KEY_INVITEM005)));
		// ressources
		Assert.assertEquals(true,
			nodes.contains(nodeIdMap.get(KEY_TOM)));
		Assert.assertEquals(true,
			nodes.contains(nodeIdMap.get(KEY_JOHN)));
		Assert.assertEquals(true,
			nodes.contains(nodeIdMap.get(KEY_EXPENSE)));
		Assert.assertEquals(true,
			nodes.contains(nodeIdMap.get(KEY_REVENUE)));
	    }
	}
    }
}
