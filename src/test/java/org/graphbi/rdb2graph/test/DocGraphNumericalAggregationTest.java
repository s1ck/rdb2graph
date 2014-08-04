package org.graphbi.rdb2graph.test;

import java.util.List;

import junit.framework.Assert;

import org.graphbi.rdb2graph.analysis.documentgraph.DocGraph;
import org.graphbi.rdb2graph.analysis.documentgraph.DocGraphExtractor;
import org.graphbi.rdb2graph.analysis.documentgraph.analyzer.AnalyzerResult;
import org.graphbi.rdb2graph.analysis.documentgraph.analyzer.DocGraphMeasureFunction;
import org.graphbi.rdb2graph.analysis.documentgraph.analyzer.NumericalAggregation;
import org.graphbi.rdb2graph.util.graph.impl.Neo4jGraph;
import org.graphbi.rdb2graph.util.graph.impl.analyzer.NeoDocGraphMeasureFunction;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.IteratorUtil;

public class DocGraphNumericalAggregationTest extends GraphBITest {

	@Test
	public void testDocGraphDensityAggregation() {
		DocGraphExtractor extractor = new DocGraphExtractor(new Neo4jGraph(
				graphDb), nodeClassSuperClassMap);
		List<DocGraph> docGraphs = extractor.extract();

		// calculate the density of each docgraph
		List<AnalyzerResult<Double>> result = new NumericalAggregation()
				.analyze(docGraphs, new DocGraphMeasureFunction<Double>() {

					@Override
					public Double measure(DocGraph docGraph) {
						return docGraph.getEdgeCount()
								/ Math.pow(docGraph.getNodeCount(), 2);
					}
				});

		// test
		for (AnalyzerResult<Double> r : result) {
			for (DocGraph docGraph : docGraphs) {
				if (r.getDocGraph().getId().equals(docGraph.getId())) {
					Assert.assertEquals(
							docGraph.getEdgeCount()
									/ Math.pow(docGraph.getNodeCount(), 2),
							r.getResult());
				}
			}
		}
	}

	@Test
	public void testDocGraphMaxDegreeExtraction() {
		DocGraphExtractor extractor = new DocGraphExtractor(new Neo4jGraph(
				graphDb), nodeClassSuperClassMap);
		List<DocGraph> docGraphs = extractor.extract();

		// calculate the max degree of each docgraph
		List<AnalyzerResult<Integer>> result = new NumericalAggregation()
				.analyze(docGraphs, new NeoDocGraphMeasureFunction<Integer>(
						graphDb) {

					@Override
					public Integer measure(DocGraph docGraph) {
						Integer maxDegree = 0;
						Node n = null;
						try (Transaction tx = graphDb.beginTx()) {
							for (Long nodeId : docGraph.getNodes()) {
								n = graph.getNodeById(nodeId);
								if (IteratorUtil.count(n.getRelationships()) > maxDegree) {
									maxDegree = IteratorUtil.count(n
											.getRelationships());
								}
							}
							tx.success();
						}

						return maxDegree;
					}
				});

		// test
		AnalyzerResult<Integer> res = null;
		for (DocGraph docGraph : docGraphs) {
			for (AnalyzerResult<Integer> r : result) {
				if (r.getDocGraph().getId().equals(docGraph.getId())) {
					res = r;
				}
			}
			// check if it's Sales DocGraph 1
			if (docGraph.getNodes().contains(nodeIdMap.get(KEY_Q001))) {
				Assert.assertEquals(5, (int) res.getResult());
			} else {
				// it's Sales DocGraph 2
				Assert.assertEquals(5, (int) res.getResult());
			}
		}
	}
}
