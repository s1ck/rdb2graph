package org.graphbi.rdb2graph.transformation.wrapper;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.graphbi.rdb2graph.util.config.DataSinkInfo;
import org.graphbi.rdb2graph.util.wrapper.NeoWrapper;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.impl.util.FileUtils;

public class GraphTransformationWrapperFactory {

    private static Logger log = Logger
	    .getLogger(GraphTransformationWrapperFactory.class);

    /**
     * Returns an instance of GraphTransformationWrapper based on the
     * information in the data sink.
     * 
     * @param dataSinkInfo
     * @return
     */
    public static GraphTransformationWrapper getInstance(
	    DataSinkInfo dataSinkInfo) {
	if ("neo4j".equals(dataSinkInfo.getType())) {
	    if (dataSinkInfo.getDrop()) {
		try {
		    log.info(String.format("Dropping Neo4j at %s",
			    dataSinkInfo.getPath()));
		    FileUtils
			    .deleteRecursively(new File(dataSinkInfo.getPath()));
		} catch (IOException e) {
		    e.printStackTrace();
		}
	    }
	    GraphDatabaseService graphdb = new GraphDatabaseFactory()
		    .newEmbeddedDatabase(dataSinkInfo.getPath());
	    registerShutdownHook(graphdb);
	    log.info("Initialized Neo4j");
	    return new NeoWrapper(graphdb);
	} else {
	    throw new IllegalArgumentException(
		    "Only Neo4j is currently supported as datasink");
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
