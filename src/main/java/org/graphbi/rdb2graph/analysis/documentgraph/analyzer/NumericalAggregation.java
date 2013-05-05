package org.graphbi.rdb2graph.analysis.documentgraph.analyzer;

import java.util.ArrayList;
import java.util.List;

import org.graphbi.rdb2graph.analysis.documentgraph.DocGraph;

public class NumericalAggregation {

    public <T extends Number> List<AnalyzerResult<T>> analyze(List<DocGraph> docGraps,
	    DocGraphMeasureFunction<T> measure, DocGraphFilterFunction f) {
	List<AnalyzerResult<T>> result = new ArrayList<AnalyzerResult<T>>();
	for (DocGraph docGraph : docGraps) {
	    if (f == null || (f != null && f.filter(docGraph))) {
		result.add(new AnalyzerResult<T>(docGraph, measure
			.measure(docGraph)));
	    }
	}
	return result;
    }

    public <T extends Number> List<AnalyzerResult<T>> analyze(List<DocGraph> docGraphs,
	    DocGraphMeasureFunction<T> measure) {
	return analyze(docGraphs, measure, null);
    }
}
