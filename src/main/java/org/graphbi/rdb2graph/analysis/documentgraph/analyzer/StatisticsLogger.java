package org.graphbi.rdb2graph.analysis.documentgraph.analyzer;

import java.util.List;

import org.apache.log4j.Logger;
import org.graphbi.rdb2graph.analysis.documentgraph.DocGraph;

public class StatisticsLogger {

    private static Logger log = Logger.getLogger(StatisticsLogger.class);

    public void analyze(List<DocGraph> docGraphs) {
	analyze(docGraphs, null);
    }

    public void analyze(List<DocGraph> docGraphs, DocGraphFilterFunction filter) {
	log.info(String.format("Analyzing %d document graphs", docGraphs.size()));
	int n = docGraphs.size();
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

	for (DocGraph docGraph : docGraphs) {
	    if (filter != null && !filter.filter(docGraph)) {
		continue;
	    }
	    localNodeCount = docGraph.getNodes().size();
	    localEdgeCount = docGraph.getEdges().size();
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

	log.info(String.format("Analyzed %d document graphs.", n));
	log.info(String.format("Average node count: %.2f", avgNodeCount));
	log.info(String.format("Average edge count: %.2f", avgEdgeCount));
	log.info(String.format("Maximum node count: %d", maxNodeCount));
	log.info(String.format("Maximum edge count: %d", maxEdgeCount));
	log.info(String.format("Minimum node count: %d", minNodeCount));
	log.info(String.format("Minimum edge count: %d", minEdgeCount));
    }
}
