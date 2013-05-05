package org.graphbi.rdb2graph.analysis.documentgraph.analyzer;

import org.graphbi.rdb2graph.analysis.documentgraph.DocGraph;

public interface DocGraphAnalyzer<T extends Number> {
    T analyze(DocGraph docGraph, DocGraphMeasureFunction<T> func);
}
