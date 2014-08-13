package org.graphbi.rdb2graph.util.graph;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.graphbi.rdb2graph.util.config.DataSinkInfo;
import org.graphbi.rdb2graph.util.graph.impl.Neo4jGraph;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.impl.util.FileUtils;

public class ReadWriteGraphFactory {
	private static Logger log = Logger.getLogger(ReadWriteGraphFactory.class);

	/**
	 * Returns an instance of GraphTransformationWrapper based on the
	 * information in the data sink.
	 * 
	 * @param dataSinkInfo
	 * @return
	 */
	public static ReadWriteGraph getInstance(DataSinkInfo dataSinkInfo) {
		if (dataSinkInfo == null) {
			throw new IllegalArgumentException(
					"No datasink information defined.");
		}
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
			return new Neo4jGraph(graphdb);
		} else {
			throw new IllegalArgumentException("Configured graph database '"
					+ dataSinkInfo.getType()
					+ "' is not supported for read and write operations.");
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
