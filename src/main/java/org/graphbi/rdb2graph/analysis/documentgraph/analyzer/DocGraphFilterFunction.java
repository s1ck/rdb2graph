package org.graphbi.rdb2graph.analysis.documentgraph.analyzer;

import org.graphbi.rdb2graph.analysis.documentgraph.DocGraph;

public interface DocGraphFilterFunction {
    boolean filter(DocGraph docGraph);
}
