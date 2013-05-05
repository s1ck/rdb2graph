package org.graphbi.rdb2graph.analysis.documentgraph.analyzer;

import org.graphbi.rdb2graph.analysis.documentgraph.DocGraph;

public interface DocGraphMeasureFunction<T> {
    T measure(DocGraph docGraph);
}
