package org.graphbi.rdb2graph.analysis.operationgraph;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.event.ListSelectionEvent;

import org.apache.ddlutils.model.Database;
import org.apache.log4j.Logger;
import org.graphbi.rdb2graph.util.graph.ReadOnlyGraph;

public class OperationGraphAnalyzer {

    private static Logger log = Logger.getLogger(OperationGraphAnalyzer.class);

    @SuppressWarnings("unused")
    private final Database relationalDB;
    @SuppressWarnings("unused")
    private final ReadOnlyGraph graphDB;

    public OperationGraphAnalyzer(Database relationalDB, ReadOnlyGraph graphDB) {
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
	}

	avgNodeCount = nodeSum / (float) n;
	avgEdgeCount = edgeSum / (float) n;

	log.info(String.format("Analyzed %d operation graphs.", n));
	log.info(String.format("Average node count: %.2f", avgNodeCount));
	log.info(String.format("Average edge count: %.2f", avgEdgeCount));
	log.info(String.format("Maximum node count: %d", maxNodeCount));
	log.info(String.format("Maximum edge count: %d", maxEdgeCount));
	log.info(String.format("Minimum node count: %d", minNodeCount));
	log.info(String.format("Minimum edge count: %d", minEdgeCount));
    }
}
