package org.graphbi.rdb2graph.analysis.operationgraph;

import java.util.HashSet;
import java.util.Set;

public class OperationGraph {
    private Set<Long> nodes;

    private Set<Long> edges;

    public OperationGraph() {
	nodes = new HashSet<Long>();
	edges = new HashSet<Long>();
    }

    public void addNode(Long nodeId) {
	nodes.add(nodeId);
    }

    public void addEdge(Long edgeId) {
	edges.add(edgeId);
    }

    public Set<Long> getNodes() {
	return nodes;
    }

    public Set<Long> getEdges() {
	return edges;
    }
}
