package org.graphbi.sql2neo.transformer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Reference;
import org.apache.ddlutils.model.Table;
import org.apache.log4j.Logger;
import org.graphbi.sql2neo.util.LinkTableInfo;
import org.graphbi.sql2neo.wrapper.Wrapper;

import scala.actors.threadpool.Arrays;

public class Transformer {

    private static Logger log = Logger.getLogger(Transformer.class);

    public static final String ID_KEY = "__id__";
    public static final String TYPE_KEY = "__type__";
    public static final String SOURCE_KEY = "__source__";

    private final Platform platform;
    private final Database rDatabase;
    private final Wrapper gDatabase;
    private final String databaseName;

    private Map<String, LinkTableInfo> linkTableMap;

    public Transformer(Platform relPlatform, Wrapper graphdb,
	    String databaseName, List<LinkTableInfo> linkTables) {
	this.databaseName = databaseName;
	this.platform = relPlatform;
	this.gDatabase = graphdb;
	this.rDatabase = relPlatform.readModelFromDatabase(databaseName);
	this.linkTableMap = new HashMap<String, LinkTableInfo>();
	for (LinkTableInfo lt : linkTables) {
	    linkTableMap.put(lt.getLinkTable(), lt);
	}
    }

    public void transform() {
	log.info("Starting transformation");

	// tables
	transformTables(rDatabase.getTables());
	// foreign keys (1:n)
	transformForeignKeys(rDatabase.getTables());

	// iterate all tables and print the content
	// for (Table t : rdb.getTables()) {
	// System.out.println(t);
	// Column[] cols = t.getColumns();
	// List it = p.fetch(rdb, "SELECT * FROM " + t.getName());
	// for (Object o : it) {
	// DynaBean row = (DynaBean) o;
	// for (Column col : cols) {
	// System.out.print("\t" + col.getName() + ":"
	// + row.get(col.getName()));
	// }
	// System.out.println();
	// }
	// }
	log.info("Finished transformation");
    }

    private void transformTables(final Table... tables) {
	log.info(String.format("Transforming %d tables", tables.length));
	StopWatch sw = new StopWatch();
	sw.start();
	for (Table t : tables) {
	    transformTable(t);
	    // testing, just one row
	    // break;
	}
	sw.stop();
	log.info(String.format("Finished transforming %d tables, took %s",
		tables.length, sw));
    }

    /**
     * Transforms all rows of a table into a node in the graph. Primary key
     * columns are concatenated with the corresponding type and used as a node
     * identifier.
     * 
     * Foreign keys attributes are also stored and will be removed during the
     * creation of relationships.
     * 
     * Null values are currently ignored.
     * 
     * @param t
     *            Table to be transformed
     */
    @SuppressWarnings({ "rawtypes" })
    private void transformTable(final Table t) {
	log.info(String.format("Transforming %s", t));
	StopWatch sw = new StopWatch();
	sw.start();

	String tableName = t.getName();
	if (linkTableMap.containsKey(tableName)) {
	    log.info(String.format("%s is a linktable, skipping", tableName));
	    return;
	}

	// subtract the pk- and fk-columns from the whole column set
	List<Column> propertyCols = getPropertyColumns(t);

	// get the data
	Iterator it = platform.query(rDatabase, "SELECT * FROM " + tableName);

	// store non-pk properties
	Map<String, Object> properties = null;
	String primaryKey;
	DynaBean row;

	// process all rows and create nodes in one transaction
	gDatabase.beginTransaction();

	while (it.hasNext()) {
	    row = (DynaBean) it.next();
	    properties = new HashMap<String, Object>();
	    // meta
	    properties.put(SOURCE_KEY, databaseName);
	    properties.put(TYPE_KEY, tableName);

	    // concatenante the primary key
	    // <relname>_<pk_1>[_<pk_n>]*
	    primaryKey = tableName;
	    for (Column c : t.getPrimaryKeyColumns()) {
		primaryKey += "_" + row.get(c.getName());
	    }
	    properties.put(ID_KEY, primaryKey);

	    // read all non-pk properties (including foreign keys)
	    for (Column c : propertyCols) {
		// TODO: maybe set the default value if its null?
		if (row.get(c.getName()) != null) {
		    properties.put(c.getName(), row.get(c.getName()));
		}
	    }
	    // create new node based on the given properties
	    gDatabase.createNode(properties);
	}
	gDatabase.successTransaction();
	gDatabase.finishTransaction();

	sw.stop();
	log.info(String.format("Took %s", sw));
    }

    private void transformForeignKeys(final Table... tables) {
	log.info(String.format("Transforming foreign keys", tables.length));
	StopWatch sw = new StopWatch();
	sw.start();
	for (Table t : tables) {
	    if (!linkTableMap.containsKey(t.getName())) {
		for (ForeignKey fk : t.getForeignKeys()) {
		    transformForeignKey(fk, t);
		}
	    }
	}
	sw.stop();
	log.info(String.format("Finished transforming foreign keys, took %s",
		sw));
    }

    private void transformForeignKey(ForeignKey fk, Table t) {
	log.info(String.format("Transforming %s of %s", fk, t));
	StopWatch sw = new StopWatch();
	sw.start();

	List<String> fkCols = new ArrayList<String>();

	for (Reference ref : fk.getReferences()) {

	    log.info(ref);
	}

	sw.stop();
	log.info(String.format("Took %s", sw));
    }

    @SuppressWarnings("unchecked")
    private List<Column> getPropertyColumns(final Table t) {
	Column[] columns = t.getColumns();
	Column[] pkColumns = t.getPrimaryKeyColumns();
	List<Column> fkColumns = new ArrayList<Column>();
	for (ForeignKey fk : t.getForeignKeys()) {
	    for (Reference r : fk.getReferences()) {
		fkColumns.add(r.getLocalColumn());
	    }
	}
	List<Column> propertyCols = ListUtils.subtract(Arrays.asList(columns),
		Arrays.asList(pkColumns));
	propertyCols = ListUtils.subtract(propertyCols, fkColumns);

	return propertyCols;
    }
}
