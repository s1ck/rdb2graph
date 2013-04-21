package org.graphbi.rdb2graph.analysis.operationgraph;

import java.util.List;

import org.apache.ddlutils.model.Database;
import org.apache.log4j.Logger;
import org.graphbi.rdb2graph.analysis.wrapper.GraphAnalysisWrapper;

public class OperationGraphAnalyzer {

    private static Logger log = Logger.getLogger(OperationGraphAnalyzer.class);

    private final Database relationalDB;
    private final GraphAnalysisWrapper graphDB;

    public OperationGraphAnalyzer(Database relationalDB,
	    GraphAnalysisWrapper graphDB) {
	this.relationalDB = relationalDB;
	this.graphDB = graphDB;
    }

    public void analyze(List<OperationGraph> opGraphs) {
	log.info(String.format("Analyzing %d operation graphs", opGraphs.size()));
	int n = opGraphs.size();
	int localNodeCount = 0;
	int localEdgeCount = 0;
	int maxNodeCount = 0;
	int maxEdgeCount = 0;
	int minNodeCount = Integer.MAX_VALUE;
	int minEdgeCount = Integer.MAX_VALUE;
	int nodeSum = 0;
	int edgeSum = 0;
	float avgNodeCount = 0f;
	float avgEdgeCount = 0f;

	int i = 0;

	for (OperationGraph opGraph : opGraphs) {
	    localNodeCount = opGraph.getNodes().size();
	    localEdgeCount = opGraph.getEdges().size();
	    if (localNodeCount > maxNodeCount) {
		maxNodeCount = localNodeCount;
	    }
	    if (localEdgeCount > maxEdgeCount) {
		maxEdgeCount = localEdgeCount;
	    }
	    if (localNodeCount < minNodeCount) {
		minNodeCount = localNodeCount;
	    }
	    if (localEdgeCount < minEdgeCount) {
		minEdgeCount = localEdgeCount;
	    }
	    nodeSum += localNodeCount;
	    edgeSum += localEdgeCount;

	    // System.out.println(String.format("OpGraph %d", ++i));
	    // for (Long nodeId : opGraph.getNodes()) {
	    // node = gdb.getNodeById(nodeId);
	    // if (node.hasProperty(Constants.ID_KEY)) {
	    // System.out.println(String.format("\t%d \t %s",
	    // node.getId(),
	    // node.getProperty(Constants.ID_KEY)));
	    // } else {
	    // System.out.println("check this node: " + node);
	    // }
	    // }
	}

	avgNodeCount = nodeSum / (float) n;
	avgEdgeCount = edgeSum / (float) n;

	log.info(String.format("\nAnalyzed %d operation graphs.\n"
		+ "Average node count: %.2f\n" + "Average edge count: %.2f\n"
		+ "Maximum node count: %d\n" + "Maximum edge count: %d\n"
		+ "Minimum node count: %d\n" + "Minimum edge count: %d\n", n,
		avgNodeCount, avgEdgeCount, maxNodeCount, maxEdgeCount,
		minNodeCount, minEdgeCount));
    }
}
