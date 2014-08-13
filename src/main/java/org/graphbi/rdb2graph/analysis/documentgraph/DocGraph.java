package org.graphbi.rdb2graph.analysis.documentgraph;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class DocGraph implements Comparable<DocGraph> {
	private static Random r = new Random();

	private final Long id;

	private Set<Long> nodes;

	private Set<Long> edges;

	public DocGraph() {
		this(r.nextLong());
	}

	public DocGraph(final Long id) {
		this.id = id;
		nodes = new HashSet<Long>();
		edges = new HashSet<Long>();
	}

	public Long getId() {
		return id;
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
	public int compareTo(DocGraph o) {
		if (o == null) {
			throw new IllegalArgumentException("o cannot be null");
		}
		if (o instanceof DocGraph) {
			int size_a = getNodeCount();
			int size_b = ((DocGraph) o).getNodeCount();
			return (size_a < size_b) ? -1 : (size_a > size_b) ? 1 : 0;
		} else {
			throw new IllegalArgumentException("o has wrong type");
		}
	}
}
