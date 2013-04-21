package org.graphbi.rdb2graph.util.graph;

import org.apache.log4j.Logger;
import org.graphbi.rdb2graph.util.config.DataSinkInfo;
import org.graphbi.rdb2graph.util.graph.impl.Neo4jGraph;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

public class ReadOnlyGraphFactory {
    private static Logger log = Logger.getLogger(ReadOnlyGraphFactory.class);

    /**
     * Returns an instance of GraphAnalysisWrapper based on the information in
     * the data sink.
     * 
     * @param dataSinkInfo
     * @return
     */
    public static ReadOnlyGraph getInstance(DataSinkInfo dataSinkInfo) {
	if ("neo4j".equals(dataSinkInfo.getType())) {
	    GraphDatabaseService graphdb = new GraphDatabaseFactory()
		    .newEmbeddedDatabase(dataSinkInfo.getPath());
	    registerShutdownHook(graphdb);
	    log.info("Initialized Neo4j");
	    return new Neo4jGraph(graphdb);
	} else {
	    throw new IllegalArgumentException("Configured graph database '"
		    + dataSinkInfo.getType()
		    + "' is not supported for read only operations.");
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
