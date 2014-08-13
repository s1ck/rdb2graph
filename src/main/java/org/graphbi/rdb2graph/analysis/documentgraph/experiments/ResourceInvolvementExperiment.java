package org.graphbi.rdb2graph.analysis.documentgraph.experiments;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.graphbi.rdb2graph.analysis.Experiment;
import org.graphbi.rdb2graph.analysis.documentgraph.DocGraph;
import org.graphbi.rdb2graph.analysis.documentgraph.analyzer.DocGraphFilterFunction;
import org.graphbi.rdb2graph.analysis.documentgraph.analyzer.ResourceInvolvementExtractor;
import org.graphbi.rdb2graph.util.config.Constants;
import org.graphbi.rdb2graph.util.graph.ReadOnlyGraph;

public class ResourceInvolvementExperiment implements Experiment {

	private static Logger log = Logger
			.getLogger(ResourceInvolvementExperiment.class);

	private ReadOnlyGraph graph;

	private ResourceInvolvementExtractor extractor;

	private List<DocGraph> docGraphs;

	private Set<String> interestingResources;

	private Set<String> interestingDocuments;

	public ResourceInvolvementExperiment(ReadOnlyGraph graph,
			List<DocGraph> docGraphs, ResourceInvolvementExtractor extractor) {
		this.graph = graph;
		this.docGraphs = docGraphs;
		this.extractor = extractor;

		interestingResources = new HashSet<String>();
		interestingResources.add("User");
		interestingDocuments = new HashSet<String>();
		interestingDocuments.add("CustomerInvoice");
	}

	@Override
	public void run() {
		log.info("Starting experiment: Resource Involvement Extraction");
		StopWatch sw = new StopWatch();
		sw.start();
		Map<String, Set<Long>> result = extractor.extract(docGraphs,
				new DocGraphFilterFunction() {
					@Override
					public boolean filter(DocGraph docGraph) {
						Map<String, Object> props;
						for (Long nodeId : docGraph.getNodes()) {
							props = graph.getNodeProperties(nodeId);
							if (props.containsKey(Constants.CLASS_KEY)
									&& interestingDocuments.contains(props
											.get(Constants.CLASS_KEY))) {
								return true;
							}
						}
						return false;
					}
				}, interestingResources, interestingDocuments);

		sw.stop();
		log.info(String.format("Done. Found %d patterns. Took %s",
				result.size(), sw));

		// BufferedWriter bw = null;
		// try {
		// bw = new BufferedWriter(new FileWriter(new File("paths.csv")));
		// for (Map.Entry<String, Set<Long>> e : result.entrySet()) {
		// for (Long docGraphId : e.getValue()) {
		// bw.write(String.format("%d\t%s\n", docGraphId, e.getKey()));
		// }
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

		for (Map.Entry<String, Set<Long>> e : result.entrySet())

			log.info(e.getKey() + " => " + e.getValue());
	}
}
