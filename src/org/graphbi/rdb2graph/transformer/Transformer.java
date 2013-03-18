package org.graphbi.rdb2graph.transformer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Reference;
import org.apache.ddlutils.model.Table;
import org.apache.log4j.Logger;
import org.graphbi.rdb2graph.util.LinkInfo;
import org.graphbi.rdb2graph.util.LinkTableInfo;
import org.graphbi.rdb2graph.wrapper.Wrapper;

import scala.actors.threadpool.Arrays;

public class Transformer {

    private static Logger log = Logger.getLogger(Transformer.class);

    /**
     * Property keys for the Property Graph Model to store some meta about the
     * origin of the nodes and edges.
     */
    public static final String ID_KEY = "__id__";
    public static final String TYPE_KEY = "__type__";
    public static final String SOURCE_KEY = "__source__";

    private final Platform platform;
    private final Database rDatabase;
    private final Wrapper gDatabase;
    private final String databaseName;

    private long rowCnt;
    private long linkCnt;

    private Map<String, LinkTableInfo> linkTableMap;

    public Transformer(Platform relPlatform, Wrapper graphdb,
	    String databaseName, List<LinkTableInfo> linkTables) {
	this.databaseName = databaseName;
	this.platform = relPlatform;
	this.gDatabase = graphdb;
	this.rDatabase = relPlatform.readModelFromDatabase(databaseName);
	this.linkTableMap = new HashMap<String, LinkTableInfo>();

	this.rowCnt = 0;
	this.linkCnt = 0;

	for (LinkTableInfo linkTable : linkTables) {
	    linkTableMap.put(linkTable.getName(), linkTable);
	}
    }
    
    public void transform() {
	log.info("Starting transformation pipeline");
	StopWatch sw = new StopWatch();
	sw.start();

	// tables
	transformTables(rDatabase.getTables());
	// foreign keys (1:n)
	transformForeignKeys(rDatabase.getTables());
	// link tables (n:m)
	transformLinkTables(rDatabase.getTables());

	sw.stop();
	log.info(String
		.format("Finished transformation pipeline. Processed %d rows and %d links. took %s",
			rowCnt, linkCnt, sw));
    }

    /**
     * 
     * Transforms the rows of a collection of tables.
     * 
     * Link tables are not considered.
     * 
     * @param tables
     *            A collection of tables.
     */
    private void transformTables(final Table... tables) {
	log.info(String.format("Transforming %d tables", tables.length));
	StopWatch sw = new StopWatch();
	sw.start();
	for (Table t : tables) {
	    transformTable(t);
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
	DynaBean row;

	// process all rows and create nodes in one tx for better performance
	gDatabase.beginTransaction();

	while (it.hasNext()) {
	    row = (DynaBean) it.next();
	    rowCnt++;
	    properties = new HashMap<String, Object>();
	    // meta
	    properties.put(SOURCE_KEY, databaseName);
	    properties.put(TYPE_KEY, tableName);
	    properties.put(ID_KEY, getPrimaryKeyNodeValue(t, row));

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

    /**
     * Transforms the foreign keys of a collection of tables.
     * 
     * Link Tables are not considered by this method.
     * 
     * @param tables
     *            A collection of Tables
     */
    private void transformForeignKeys(final Table... tables) {
	log.info(String.format("Transforming foreign keys of %d tables",
		tables.length));
	StopWatch sw = new StopWatch();
	sw.start();
	for (Table t : tables) {
	    if (!linkTableMap.containsKey(t.getName())) {
		transformForeignKeys(t);
	    }
	}
	sw.stop();
	log.info(String.format("Finished transforming foreign keys, took %s",
		sw));
    }

    /**
     * Creates all links for the given table.
     * 
     * Method iterates the rows of the given table and creates relationships to
     * foreign rows based on the foreign key information of the table.
     * 
     * @param t
     *            Table whose links shall be created.
     */
    @SuppressWarnings("rawtypes")
    private void transformForeignKeys(final Table t) {
	log.info(String.format("Transforming foreign keys for %s", t));
	StopWatch sw = new StopWatch();
	sw.start();

	// get the relevant data
	Iterator it = platform.query(
		rDatabase,
		String.format("SELECT %s FROM %s",
			StringUtils.join(getSelectColumns(t).toArray(), ","),
			t.getName()));
	DynaBean row;
	String pkLocal, pkForeign;
	Map<String, Object> properties = null;
	Object rowValue;

	// process the creation of all links in one tx for better performance
	gDatabase.beginTransaction();
	while (it.hasNext()) {
	    row = (DynaBean) it.next();
	    // create all links for the current row
	    for (ForeignKey fk : t.getForeignKeys()) {
		for (Reference r : fk.getReferences()) {
		    // foreign id is local value (skip null values)
		    rowValue = row.get(r.getLocalColumnName());
		    if (rowValue == null) {
			continue;
		    }
		    linkCnt++;
		    properties = new HashMap<String, Object>();
		    // local node key
		    pkLocal = getPrimaryKeyNodeValue(t, row);
		    // foreign node key
		    // TODO: think about if this is correct when the referenced
		    // key is a multi-key
		    pkForeign = String.format("%s_%s",
			    fk.getForeignTableName(), rowValue);
		    properties.put(SOURCE_KEY, databaseName);
		    properties.put(TYPE_KEY, fk.getName());
		    properties.put(ID_KEY,
			    getPrimaryKeyLinkValue(fk, pkLocal, pkForeign));

		    gDatabase.createRelationship(pkLocal, pkForeign,
			    fk.getName(), properties);
		}
	    }
	}
	gDatabase.successTransaction();
	gDatabase.finishTransaction();

	sw.stop();
	log.info(String.format("Took %s", sw));
    }

    /**
     * Transforms a given collection of link tables based on the given LinkTable
     * Information
     * 
     * @param tables
     *            Collection of tables
     */
    private void transformLinkTables(final Table... tables) {
	for (Table t : tables) {
	    if (linkTableMap.containsKey(t.getName())) {
		transformLinkTable(t);
	    }
	}
    }

    /**
     * Creates relationships based on the link information in the given link
     * table.
     * 
     * @param table
     */
    @SuppressWarnings("rawtypes")
    private void transformLinkTable(final Table table) {
	log.info(String.format("Transforming link table %s", table));
	StopWatch sw = new StopWatch();
	sw.start();
	LinkTableInfo tableInfo = linkTableMap.get(table.getName());
	List<String> selectColumns = new ArrayList<String>();
	for (LinkInfo linkInfo : tableInfo.getLinkInfos()) {
	    selectColumns.add(linkInfo.getFromColumnName());
	    selectColumns.add(linkInfo.getToColumnName());
	}

	// get the relevant data
	Iterator it = platform.query(
		rDatabase,
		String.format("SELECT %s FROM %s",
			StringUtils.join(selectColumns.toArray(), ","),
			table.getName()));
	DynaBean row;
	String pkFrom, pkTo;

	gDatabase.beginTransaction();
	while (it.hasNext()) {
	    row = (DynaBean) it.next();

	    for (LinkInfo linkInfo : tableInfo.getLinkInfos()) {
		pkFrom = String.format("%s_%s", linkInfo.getFromTableName(),
			row.get(linkInfo.getFromColumnName()));
		pkTo = String.format("%s_%s", linkInfo.getToTableName(),
			row.get(linkInfo.getToColumnName()));

		// TODO: read properties for the relationship
		gDatabase.createRelationship(pkFrom, pkTo,
			linkInfo.getLinkType());
		linkCnt++;
	    }
	}
	gDatabase.successTransaction();
	gDatabase.finishTransaction();
	sw.stop();
	log.info(String.format("Took %s", sw));
    }

    /**
     * Returns a list of columns which define properties in the given table.
     * This excludes all primary key and foreign key columns.
     * 
     * @param t
     *            Table whose columns shall be retrieved.
     * @return List of columns of the given table
     */
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

    /**
     * Generates the id property for a row inside the graph.
     * 
     * <Table.Name>
     * _<PrimaryKey_1>[_<PrimaryKey_n>]*
     * 
     * @param t
     *            Row's table
     * @param row
     *            Row
     * @return Internally used primary key for the given row.
     */
    private String getPrimaryKeyNodeValue(final Table t, final DynaBean row) {
	String primaryKey = t.getName();
	for (Column c : t.getPrimaryKeyColumns()) {
	    primaryKey += "_" + row.get(c.getName());
	}
	return primaryKey;
    }

    /**
     * Generates the id property for a link between two nodes in the graph.
     * 
     * @param fk
     *            ForeignKey is used for link properties
     * @param sourceId
     *            Internal id of the source node
     * @param targetId
     *            Internal id of the target node
     * @return
     */
    private String getPrimaryKeyLinkValue(final ForeignKey fk,
	    final String sourceId, final String targetId) {
	return String.format("%s_%s_%s", fk.getName(), sourceId, targetId);

    }

    /**
     * Returns a list of all column names which are needed to build the links of
     * a table's row. This includes the primary key and the foreign key column
     * names.
     * 
     * @param t
     *            Table whose relevant columns need to be retrieved.
     * @return List of column names
     */
    private List<String> getSelectColumns(Table t) {
	List<String> selectColumns = new ArrayList<String>();
	// add primary key columns (needed to build local primary key)
	for (Column pkCol : t.getPrimaryKeyColumns()) {
	    selectColumns.add(pkCol.getName());
	}
	// add foreign key columns
	for (ForeignKey fk : t.getForeignKeys()) {
	    for (Reference r : fk.getReferences()) {
		selectColumns.add(r.getLocalColumnName());
	    }
	}
	return selectColumns;
    }
}
