package org.graphbi.rdb2graph.analysis.wrapper;

import org.apache.log4j.Logger;
import org.graphbi.rdb2graph.util.config.DataSinkInfo;
import org.graphbi.rdb2graph.util.wrapper.NeoWrapper;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

public class GraphAnalysisWrapperFactory {
    private static Logger log = Logger
	    .getLogger(GraphAnalysisWrapperFactory.class);

    /**
     * Returns an instance of GraphAnalysisWrapper based on the information in
     * the data sink.
     * 
     * @param dataSinkInfo
     * @return
     */
    public static GraphAnalysisWrapper getInstance(DataSinkInfo dataSinkInfo) {
	if ("neo4j".equals(dataSinkInfo.getType())) {
	    GraphDatabaseService graphdb = new GraphDatabaseFactory()
		    .newEmbeddedDatabase(dataSinkInfo.getPath());
	    registerShutdownHook(graphdb);
	    log.info("Initialized Neo4j");
	    return new NeoWrapper(graphdb);
	} else {
	    throw new IllegalArgumentException(
		    "Only Neo4j is currently supported for analyzing.");
	}
    }

    /**
     * Used by Neo4j for a clean shutdown.
     * 
     * @param graphdb
     */
    private static void registerShutdownHook(final GraphDatabaseService graphdb) {
	Runtime.getRuntime().addShutdownHook(new Thread() {
	    @Override
	    public void run() {
		graphdb.shutdown();
	    }
	});
    }

}
