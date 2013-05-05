package org.graphbi.rdb2graph.analysis.documentgraph.analyzer;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.graphbi.rdb2graph.analysis.documentgraph.DocGraph;

public interface ResourceInvolvementExtractor {
    Map<String, Set<Long>> extract(List<DocGraph> docGraphs,
	    DocGraphFilterFunction filterFunc, Set<String> resourceClasses,
	    Set<String> documentClasses);

}
