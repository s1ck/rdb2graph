package org.graphbi.rdb2graph.analysis.documentgraph.analyzer;

import org.graphbi.rdb2graph.analysis.documentgraph.DocGraph;

public class AnalyzerResult<T extends Number> {
    private final DocGraph docGraph;

    private final T result;

    public AnalyzerResult(final DocGraph graph, final T result) {
	this.docGraph = graph;
	this.result = result;
    }

    public DocGraph getDocGraph() {
	return docGraph;
    }

    public T getResult() {
	return result;
    }
}
