package org.graphbi.rdb2graph.analysis.operationgraph;

import java.util.HashSet;
import java.util.Set;

public class OperationGraph implements Comparable<OperationGraph>{
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
    
    public int getNodeCount() {
	return nodes.size();
    }
    
    public int getEdgeCount() {
	return edges.size();
    }

    @Override
    public int compareTo(OperationGraph o) {
	if (o == null) {
	    throw new IllegalArgumentException("o cannot be null");
	}
	if (o instanceof OperationGraph) {
	    int size_a = getNodeCount();
	    int size_b = ((OperationGraph) o).getNodeCount();
	    return (size_a < size_b) ? -1 : (size_a > size_b) ? 1 : 0; 
	} else {
	    throw new IllegalArgumentException("o has wrong type");
	}
    }
}
