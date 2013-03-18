package org.graphbi.rdb2graph;

import java.io.File;
import java.io.IOException;

import javax.sql.DataSource;

import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.log4j.Logger;
import org.graphbi.rdb2graph.transformer.Transformer;
import org.graphbi.rdb2graph.util.Config;
import org.graphbi.rdb2graph.util.DataSinkInfo;
import org.graphbi.rdb2graph.util.DataSourceInfo;
import org.graphbi.rdb2graph.wrapper.NeoWrapper;
import org.graphbi.rdb2graph.wrapper.Wrapper;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.impl.util.FileUtils;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

public class TransformerApp {

    private static Logger log = Logger.getLogger(TransformerApp.class);

    private static void registerShutdownHook(final GraphDatabaseService graphdb) {
	Runtime.getRuntime().addShutdownHook(new Thread() {
	    @Override
	    public void run() {
		graphdb.shutdown();
	    }
	});
    }

    public static Platform initRDB(DataSourceInfo dataSourceInfo) {
	DataSource ds = null;
	Platform p = null;
	if (dataSourceInfo.getType().equals("mysql")) {
	    MysqlDataSource mysqlDs = new MysqlDataSource();
	    mysqlDs.setServerName(dataSourceInfo.getHost());
	    mysqlDs.setPort(dataSourceInfo.getPort());
	    mysqlDs.setUser(dataSourceInfo.getUser());
	    mysqlDs.setPassword(dataSourceInfo.getPassword());
	    mysqlDs.setDatabaseName(dataSourceInfo.getDatabase());
	    ds = (DataSource) mysqlDs;
	} else {
	    throw new IllegalArgumentException(
		    "Only MySQL is currently supported");
	}
	p = PlatformFactory.createNewPlatformInstance(ds);
	log.info(String.format("Initialized %s platform",
		dataSourceInfo.getType()));

	// would be a nicer generic solution, but the platform doesn't create a
	// datasource internally
	// Platform p = PlatformFactory
	// .createNewPlatformInstance("com.mysql.jdbc.Driver",
	// "jdbc:mysql://localhost/sakila?user=graphbi&password=graphbi");

	return p;
    }

    public static Wrapper initGDB(DataSinkInfo dataSinkInfo) {
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

    public static void main(String[] args) throws IOException {
	if (args.length == 0) {
	    throw new IllegalArgumentException("missing config path");
	}
	Config cfg = new Config(args[0]);
	cfg.parse();

	DataSourceInfo ds = cfg.getDataSource();
	Platform source = initRDB(ds);

	Wrapper sink = initGDB(cfg.getDataSink());

	Transformer t = new Transformer(source, sink, ds.getDatabase(),
		cfg.getLinkTableInfos());

	t.transform();
    }
}
