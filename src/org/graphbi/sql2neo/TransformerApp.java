package org.graphbi.sql2neo;

import java.io.File;
import java.io.IOException;

import javax.sql.DataSource;

import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.log4j.Logger;
import org.graphbi.sql2neo.transformer.Transformer;
import org.graphbi.sql2neo.util.Config;
import org.graphbi.sql2neo.util.DataSourceInfo;
import org.graphbi.sql2neo.wrapper.NeoWrapper;
import org.graphbi.sql2neo.wrapper.Wrapper;
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

    public static Platform initRDB(DataSourceInfo datasourceInfo) {
	DataSource ds = null;
	if (datasourceInfo.getType().equals("mysql")) {
	    MysqlDataSource mysqlDs = new MysqlDataSource();
	    mysqlDs.setServerName(datasourceInfo.getHost());
	    mysqlDs.setPort(datasourceInfo.getPort());
	    mysqlDs.setUser(datasourceInfo.getUser());
	    mysqlDs.setPassword(datasourceInfo.getPassword());
	    mysqlDs.setDatabaseName(datasourceInfo.getDatabase());
	    ds = (DataSource) mysqlDs;
	} else {
	    throw new IllegalArgumentException(
		    "Only MySQL is currently supported");
	}
	Platform p = PlatformFactory.createNewPlatformInstance(ds);
	// would be a nicer generic solution, but the platform doesn't create a
	// datasource internally
	// Platform p = PlatformFactory
	// .createNewPlatformInstance("com.mysql.jdbc.Driver",
	// "jdbc:mysql://localhost/sakila?user=graphbi&password=graphbi");
	log.info(String.format("Initialized %s platform",
		datasourceInfo.getType()));
	return p;
    }

    public static Wrapper initNeo4j(String path) {
	GraphDatabaseService graphdb = new GraphDatabaseFactory()
		.newEmbeddedDatabase(path);
	registerShutdownHook(graphdb);
	log.info("Initialized Neo4j");
	return new NeoWrapper(graphdb);
    }

    public static void shutdownNeo4j(Wrapper neo4j) {
	((NeoWrapper) neo4j).getGraphDB().shutdown();
    }

    public static void main(String[] args) throws IOException {
	String path = "target/neo4j";
	Config cfg = new Config("cfg/config.xml");
	cfg.parse();

	FileUtils.deleteRecursively(new File(path));

	DataSourceInfo ds = cfg.getDataSource();
	Platform platform = initRDB(ds);

	Wrapper neo4j = initNeo4j(path);

	Transformer t = new Transformer(platform, neo4j, ds.getDatabase(),
		cfg.getLinkTables());

	t.transform();

	shutdownNeo4j(neo4j);
    }
}
