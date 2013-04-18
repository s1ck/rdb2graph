package org.graphbi.rdb2graph.test;

import java.util.List;

import junit.framework.Assert;

import org.graphbi.rdb2graph.analysis.operationgraph.OperationGraph;
import org.graphbi.rdb2graph.analysis.operationgraph.OperationGraphExtractor;
import org.graphbi.rdb2graph.util.config.Constants;
import org.graphbi.rdb2graph.util.wrapper.NeoWrapper;
import org.junit.Test;
import org.neo4j.graphdb.Node;

public class OperationGraphAnalysisTest extends GraphBITest {

    @Test
    public void testOperationGraphExtraction() {
	OperationGraphExtractor extractor = new OperationGraphExtractor(
		new NeoWrapper(graphDb), nodeClassSuperClassMap);
	List<OperationGraph> opGraphs = extractor.extract();

	Assert.assertEquals(2, opGraphs.size());
	Node n;
	for (OperationGraph opGraph : opGraphs) {
	    System.out.println("\nopgraph\n");
	    for (Long nodeId : opGraph.getNodes()) {
		n = graphDb.getNodeById(nodeId);
		System.out.println(n.getProperty(Constants.CLASS_KEY) + " => ["
			+ n.getProperty(Constants.ID_KEY) + "]");
	    }
	}
    }
}
