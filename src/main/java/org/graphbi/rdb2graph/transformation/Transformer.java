package org.graphbi.rdb2graph.transformation;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.graphbi.rdb2graph.util.config.Config;
import org.graphbi.rdb2graph.util.config.Constants;
import org.graphbi.rdb2graph.util.graph.ReadWriteGraph;

public class Transformer {

    private static Logger log = Logger.getLogger(Transformer.class);

    /**
     * Property keys for the Property Graph Model to store some meta about the
     * origin of the nodes and edges.
     */

    private final Platform platform;
    private final Database relDatabase;
    private final ReadWriteGraph graphDatabase;

    private long rowCnt;
    private long linkCnt;

    private Map<String, Long> nodeIdx;
    private Map<String, Long> edgeIdx;

    private final boolean useSchema;
    private final boolean useDelimiters;

    public Transformer(Platform platform, Database relDatabase,
	    ReadWriteGraph graphDatabase) {
	this(platform, relDatabase, graphDatabase, false, false);
    }

    public Transformer(Platform platform, Database relDatabase,
	    ReadWriteGraph graphDatabase, boolean useSchema,
	    boolean useDelimiters) {
	this.platform = platform;
	this.relDatabase = relDatabase;
	this.graphDatabase = graphDatabase;

	this.rowCnt = 0;
	this.linkCnt = 0;

	this.nodeIdx = new HashMap<String, Long>();
	this.edgeIdx = new HashMap<String, Long>();

	this.useSchema = useSchema;
	this.useDelimiters = useDelimiters;
    }

    public void transform() {
	log.info("Starting transformation pipeline");
	StopWatch sw = new StopWatch();
	sw.start();

	// tables
	transformTables(relDatabase.getTables());
	// foreign keys (1:n)
	transformForeignKeys(relDatabase.getTables());

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
	    if (!t.isIgnored()) {
		transformTable(t);
	    }
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
     * @param table
     *            Table to be transformed
     */
    @SuppressWarnings({ "rawtypes" })
    private void transformTable(final Table table) {
	log.info(String.format("Transforming %s", table));
	int rowsPassed = 0;
	int rowsFailed = 0;
	StopWatch sw = new StopWatch();
	sw.start();

	String tableName = getFormattedTableName(table);

	// subtract the pk- and fk-columns from the whole column set
	List<Column> propertyCols = getPropertyColumns(table);

	// get a list of all columns in the model to query
	List<String> colNames = new ArrayList<String>();
	for (Column col : table.getColumns()) {
	    if (!col.isIgnored()) {
		colNames.add(getFormattedColumnName(col));
	    }
	}

	// get the data
	Iterator it = platform.query(
		relDatabase,
		String.format("SELECT %s FROM %s",
			StringUtils.join(colNames.iterator(), ","), tableName));

	// store non-pk properties
	Map<String, Object> properties = null;
	DynaBean row = null;
	Long nodeId = null;
	String primaryNodeKey = null;

	// process all rows and create nodes in one tx for better performance
	graphDatabase.beginTransaction();

	while (it.hasNext()) {
	    row = (DynaBean) it.next();
	    properties = new HashMap<String, Object>();
	    // meta
	    properties.put(Constants.SOURCE_KEY, relDatabase.getName());
	    properties.put(Constants.CLASS_KEY, getTableIdentifier(table));
	    primaryNodeKey = getPrimaryNodeKeyValue(table, row);
	    properties.put(Constants.ID_KEY, primaryNodeKey);

	    // read all non-pk properties (including foreign keys)
	    for (Column c : propertyCols) {
		// TODO: maybe set the default value if its null?
		if (row.get(c.getName()) != null) {
		    properties.put(c.getName(), row.get(c.getName()));
		}
	    }
	    // create new node based on the given properties
	    if ((nodeId = graphDatabase.createNode(properties)) != null) {
		nodeIdx.put(primaryNodeKey, nodeId);
		rowCnt++;
		rowsPassed++;
	    } else {
		rowsFailed++;
	    }
	}
	graphDatabase.successTransaction();
	graphDatabase.finishTransaction();

	sw.stop();
	log.info(String.format("Took %s Rows passed: %d Rows failed: %s", sw,
		rowsPassed, rowsFailed));
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
	    if (!t.isIgnored()) {
		if (t.getForeignKeyCount() > 0) {
		    transformForeignKeys(t);
		}
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
     * @param table
     *            Table whose links shall be created.
     */
    @SuppressWarnings("rawtypes")
    private void transformForeignKeys(final Table table) {
	log.info(String.format("Transforming foreign keys for %s", table));
	int linksPassed = 0;
	int linksFailed = 0;
	StopWatch sw = new StopWatch();
	sw.start();

	// get the relevant data
	String query = String.format("SELECT %s FROM %s",
		StringUtils.join(getLinkColumns(table).iterator(), ","),
		getFormattedTableName(table));
	Iterator it = platform.query(relDatabase, query);
	DynaBean row = null;
	String pkLocal = null, pkForeign = null;
	Map<String, Object> properties = null;
	String foreignIdString = null;
	Object tmpValue = null;
	String primaryEdgeKey = null;
	Long edgeId = null;
	int i = 0;

	// process the creation of all links in one tx for better performance
	graphDatabase.beginTransaction();
	while (it.hasNext()) {
	    row = (DynaBean) it.next();
	    // create all links for the current row
	    // local node key
	    pkLocal = getPrimaryNodeKeyValue(table, row);
	    for (ForeignKey fk : table.getForeignKeys()) {
		foreignIdString = "";
		i = 0;
		// concat all relevant IDs
		// NOTE: cannot use getPrimaryKeyNodeValue() for foreign table
		// here, because the relevant column values could be null
		for (Reference r : fk.getReferences()) {
		    tmpValue = row.get(r.getLocalColumnName());
		    if (tmpValue != null) {
			if (i > 0) {
			    foreignIdString += "_";
			}
			foreignIdString += tmpValue.toString();
			i++;
		    }
		}

		if (!"".equals(foreignIdString)) {
		    properties = new HashMap<String, Object>();
		    // foreign node key
		    pkForeign = String.format("%s_%s",
			    formatKeyCandidate(getTableIdentifier(fk
				    .getForeignTable())), foreignIdString);
		    properties.put(Constants.SOURCE_KEY, relDatabase.getName());
		    properties.put(Constants.CLASS_KEY,
			    getForeignKeyIdentifier(fk));
		    primaryEdgeKey = getPrimaryEdgeKeyValue(fk, pkLocal,
			    pkForeign);
		    properties.put(Constants.ID_KEY, primaryEdgeKey);
		    if ((edgeId = graphDatabase.createRelationship(
			    nodeIdx.get(pkLocal), nodeIdx.get(pkForeign),
			    fk.getEdgeClass(), properties)) != null) {
			edgeIdx.put(primaryEdgeKey, edgeId);
			linkCnt++;
			linksPassed++;
		    } else {
			linksFailed++;
		    }
		}
	    }
	}
	graphDatabase.successTransaction();
	graphDatabase.finishTransaction();

	sw.stop();
	log.info(String.format("Took %s Links passed: %d Links failed: %d", sw,
		linksPassed, linksFailed));
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
     * [<schemaName>.<tableName>] _<PrimaryKey_1>[_<PrimaryKey_n>]*
     * 
     * @param t
     *            Row's table
     * @param row
     *            Row
     * @return Internally used primary key for the given row.
     */
    private String getPrimaryNodeKeyValue(final Table t, final DynaBean row) {
	String primaryKey = formatKeyCandidate(getTableIdentifier(t));
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
    private String getPrimaryEdgeKeyValue(final ForeignKey fk,
	    final String sourceId, final String targetId) {
	return String.format("%s_%s_%s", getForeignKeyIdentifier(fk), sourceId,
		targetId);

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
    private List<String> getLinkColumns(Table t) {
	List<String> selectColumns = new ArrayList<String>();
	// add primary key columns (needed to build local primary key)
	for (Column pkCol : t.getPrimaryKeyColumns()) {
	    selectColumns.add(addDelimiters(pkCol.getName()));
	}
	// add foreign key columns
	for (ForeignKey fk : t.getForeignKeys()) {
	    for (Reference r : fk.getReferences()) {
		selectColumns.add(addDelimiters(r.getLocalColumnName()));
	    }
	}
	return selectColumns;
    }

    /**
     * Concatenates schema and table if necessary. Delimiters are added if
     * necessary.
     * 
     * @param table
     *            The table whose name shall be formatted
     * @return The formatted table name
     */
    private String getFormattedTableName(final Table table) {
	StringBuilder sb = new StringBuilder();

	if (useSchema && table.getSchema() != null) {
	    sb.append(addDelimiters(table.getSchema())
		    + Config.DELIMITER_CONCAT);
	}
	sb.append(addDelimiters(table.getName()));
	return sb.toString();
    }

    private String getFormattedColumnName(final Column column) {
	return addDelimiters(column.getName());
    }

    /**
     * Adds delimiters to the given string if necessary. Is used for schema,
     * table and column names.
     * 
     * @param s
     *            A string
     * @return Formatted string
     */
    private String addDelimiters(final String s) {
	if (useDelimiters) {
	    return platform.getPlatformInfo().getDelimiterToken() + s
		    + platform.getPlatformInfo().getDelimiterToken();
	} else {
	    return s;
	}
    }

    /**
     * Does some formatting for key identifiers for nodes and edges.
     * 
     * @param keyCandidate
     * @return Formatted key candidate.
     */
    private String formatKeyCandidate(String keyCandidate) {
	return keyCandidate.replaceAll(" ", "_");
    }

    /**
     * Returns the identifier for this table. If there is a nodeClass defined
     * for this table, this will be returned, else the table's name will be used
     * as an identifier.
     * 
     * @param t
     *            The table.
     * @return The table's identifier.
     */
    private String getTableIdentifier(Table t) {
	return (t.getNodeClass() != null) ? t.getNodeClass() : t.getName();
    }

    /**
     * Returns the identifier for this foreign key. If there is a edgeClass
     * defined for this foreign key, this will be returned, else the forein
     * key's name will be used as an identifier.
     * 
     * @param fk
     *            The foreign key.
     * @return The foreign key's identifier.
     */
    private String getForeignKeyIdentifier(ForeignKey fk) {
	return (fk.getEdgeClass() != null) ? fk.getEdgeClass() : fk.getName();
    }
}
