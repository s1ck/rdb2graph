package org.graphbi.rdb2graph.analysis;

import org.apache.ddlutils.model.Database;
import org.apache.log4j.Logger;
import org.graphbi.rdb2graph.analysis.wrapper.GraphAnalysisWrapper;

public class CaseGraphAnalyzer {
    private static Logger log = Logger.getLogger(CaseGraphAnalyzer.class);

    private final GraphAnalysisWrapper graphDB;
    private final Database relationalDB;

    public CaseGraphAnalyzer(Database relationalModel,
	    GraphAnalysisWrapper graphWrapper) {
	if (relationalModel == null) {
	    throw new IllegalArgumentException(
		    "relationalModel must not be null.");
	}
	if (graphWrapper == null) {
	    throw new IllegalArgumentException("graphWrapper must not be null.");
	}
	this.relationalDB = relationalModel;
	this.graphDB = graphWrapper;

    }

    public void analyze() {
	log.info("Analyzing...");
	// todo
	log.info("Done.");
    }
}
