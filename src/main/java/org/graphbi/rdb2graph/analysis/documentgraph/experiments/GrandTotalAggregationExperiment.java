package org.graphbi.rdb2graph.analysis.documentgraph.experiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.graphbi.rdb2graph.analysis.Experiment;
import org.graphbi.rdb2graph.analysis.documentgraph.DocGraph;
import org.graphbi.rdb2graph.analysis.documentgraph.analyzer.AnalyzerResult;
import org.graphbi.rdb2graph.analysis.documentgraph.analyzer.DocGraphFilterFunction;
import org.graphbi.rdb2graph.analysis.documentgraph.analyzer.DocGraphMeasureFunction;
import org.graphbi.rdb2graph.analysis.documentgraph.analyzer.NumericalAggregation;
import org.graphbi.rdb2graph.util.config.Constants;
import org.graphbi.rdb2graph.util.graph.ReadOnlyGraph;

public class GrandTotalAggregationExperiment implements Experiment {

    private static Logger log = Logger
	    .getLogger(GrandTotalAggregationExperiment.class);

    private final List<DocGraph> docGraphs;

    private final ReadOnlyGraph graphWrapper;

    // C_R - resource classes of interest
    private final Set<String> c_R;
    // C_D - document classes of interest
    private final Set<String> c_D;

    public GrandTotalAggregationExperiment(ReadOnlyGraph graph,
	    List<DocGraph> docGraphs) {
	this.docGraphs = docGraphs;
	c_R = new HashSet<String>();
	c_R.add("User");
	c_D = new HashSet<String>();
	c_D.add("CustomerInvoice");

	this.graphWrapper = graph;
    }

    @Override
    public void run() {
	List<AnalyzerResult<Double>> results = new NumericalAggregation()
		.analyze(docGraphs, new DocGraphMeasureFunction<Double>() {

		    @Override
		    public Double measure(DocGraph docGraph) {
			Map<String, Object> props;
			Double result = .0;
			for (Long nodeId : docGraph.getNodes()) {
			    props = graphWrapper.getNodeProperties(nodeId);
			    if (props.containsKey(Constants.CLASS_KEY)
				    && c_D.contains(props
					    .get(Constants.CLASS_KEY))
				    && props.containsKey("grand_total")) {
				result += Double.parseDouble((String) props
					.get("grand_total"));
			    }
			}
			return result;
		    }
		}, new DocGraphFilterFunction() {

		    @Override
		    public boolean filter(DocGraph docGraph) {
			Map<String, Object> props;
			for (Long nodeId : docGraph.getNodes()) {
			    props = graphWrapper.getNodeProperties(nodeId);
			    if (props.containsKey(Constants.CLASS_KEY)
				    && c_D.contains(props
					    .get(Constants.CLASS_KEY))) {
				return true;
			    }
			}
			return false;
		    }
		});
	
	// BufferedWriter bw = null;
	// try {
	// bw = new BufferedWriter(new FileWriter(new File("measure.csv")));
	// for (AnalyzerResult<Double> res : results) {
	// bw.write(String.format("%d\t%.2f\n", res.getDocGraph().getId(),
	// res.getResult()));
	// }
	// bw.flush();
	// } catch (Exception e) {
	//
	// } finally {
	// try {
	// bw.close();
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }

	for (AnalyzerResult<Double> res : results) {
	    log.info(String.format("%d (%d, %d) => %.2f", res.getDocGraph()
		    .getId(), res.getDocGraph().getNodeCount(), res
		    .getDocGraph().getEdgeCount(), res.getResult()));
	}
    }
}
