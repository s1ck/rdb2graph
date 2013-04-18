package org.graphbi.rdb2graph.test;

import java.util.HashMap;
import java.util.Map;

import org.apache.ddlutils.model.Database;
import org.graphbi.rdb2graph.util.config.Constants;
import org.graphbi.rdb2graph.util.config.NodeSuperClass;
import org.junit.After;
import org.junit.Before;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;

public class GraphBITest {

    protected final static String NODE_CLASS_QUOTATION = "Quotation";
    protected final static String NODE_CLASS_INVOICE = "Invoice";
    protected final static String NODE_CLASS_INVOICE_ITEM = "InvoiceItem";
    protected final static String NODE_CLASS_USER = "User";
    protected final static String NODE_CLASS_GLACCOUNT = "GLACCOUNT";
    protected final static String NODE_CLASS_SALES_ORDER = "SalesOrder";

    protected static Map<String, NodeSuperClass> nodeClassSuperClassMap = new HashMap<String, NodeSuperClass>();

    static {
	nodeClassSuperClassMap.put(NODE_CLASS_GLACCOUNT,
		NodeSuperClass.RESOURCE);
	nodeClassSuperClassMap.put(NODE_CLASS_INVOICE, NodeSuperClass.DOCUMENT);
	nodeClassSuperClassMap.put(NODE_CLASS_INVOICE_ITEM,
		NodeSuperClass.DOCUMENT);
	nodeClassSuperClassMap.put(NODE_CLASS_QUOTATION,
		NodeSuperClass.DOCUMENT);
	nodeClassSuperClassMap.put(NODE_CLASS_SALES_ORDER,
		NodeSuperClass.DOCUMENT);
	nodeClassSuperClassMap.put(NODE_CLASS_USER, NodeSuperClass.RESOURCE);
    }

    protected static enum RelTypes implements RelationshipType {
	basedOn, bills, causedBy, sentBy, createdBy, hasSupervisor, processedBy, postedOn
    }

    protected GraphDatabaseService graphDb;
    protected Database relationalDB;

    @Before
    public void prepareTestDatabase() {
	graphDb = new TestGraphDatabaseFactory()
		.newImpermanentDatabaseBuilder().newGraphDatabase();
	relationalDB = new Database();

	createSampleGraph();
    }

    protected void createSampleGraph() {
	Transaction tx = graphDb.beginTx();

	try {
	    /** Sales Case No 1 */
	    // QNo: Q001
	    Node q001 = graphDb.createNode();
	    q001.setProperty(Constants.CLASS_KEY, NODE_CLASS_QUOTATION);
	    q001.setProperty(Constants.ID_KEY, "Q001");
	    // SONo: S001
	    Node s001 = graphDb.createNode();
	    s001.setProperty(Constants.CLASS_KEY, NODE_CLASS_SALES_ORDER);
	    s001.setProperty(Constants.ID_KEY, "S001");
	    // INo: I001
	    Node i001 = graphDb.createNode();
	    i001.setProperty(Constants.CLASS_KEY, NODE_CLASS_INVOICE);
	    i001.setProperty(Constants.ID_KEY, "I001");
	    // InvoiceItem InvItem001
	    Node invItem001 = graphDb.createNode();
	    invItem001
		    .setProperty(Constants.CLASS_KEY, NODE_CLASS_INVOICE_ITEM);
	    invItem001.setProperty(Constants.ID_KEY, "InvItem001");
	    invItem001.setProperty("Amount", 10000);
	    // InvoiceItem InvItem002
	    Node invItem002 = graphDb.createNode();
	    invItem002
		    .setProperty(Constants.CLASS_KEY, NODE_CLASS_INVOICE_ITEM);
	    invItem002.setProperty(Constants.ID_KEY, "InvItem002");
	    invItem002.setProperty("Amount", -5000);

	    /** Sales Case No 2 */
	    // QNo: Q002
	    Node q002 = graphDb.createNode();
	    q002.setProperty(Constants.CLASS_KEY, NODE_CLASS_QUOTATION);
	    q002.setProperty(Constants.ID_KEY, "Q002");
	    // SONo: S002
	    Node s002 = graphDb.createNode();
	    s002.setProperty(Constants.CLASS_KEY, NODE_CLASS_SALES_ORDER);
	    s002.setProperty(Constants.ID_KEY, "S002");
	    // INo: I002
	    Node i002 = graphDb.createNode();
	    i002.setProperty(Constants.CLASS_KEY, NODE_CLASS_INVOICE);
	    i002.setProperty(Constants.ID_KEY, "I002");
	    // INo: I003
	    Node i003 = graphDb.createNode();
	    i003.setProperty(Constants.CLASS_KEY, NODE_CLASS_INVOICE);
	    i003.setProperty(Constants.ID_KEY, "I003");
	    // InvoiceItem InvItem003
	    Node invItem003 = graphDb.createNode();
	    invItem003
		    .setProperty(Constants.CLASS_KEY, NODE_CLASS_INVOICE_ITEM);
	    invItem003.setProperty(Constants.ID_KEY, "InvItem003");
	    invItem003.setProperty("Amount", -7000);
	    // InvoiceItem InvItem004
	    Node invItem004 = graphDb.createNode();
	    invItem004
		    .setProperty(Constants.CLASS_KEY, NODE_CLASS_INVOICE_ITEM);
	    invItem004.setProperty(Constants.ID_KEY, "InvItem004");
	    invItem004.setProperty("Amount", 12000);
	    // InvoiceItem InvItem005
	    Node invItem005 = graphDb.createNode();
	    invItem005
		    .setProperty(Constants.CLASS_KEY, NODE_CLASS_INVOICE_ITEM);
	    invItem005.setProperty(Constants.ID_KEY, "InvItem005");
	    invItem005.setProperty("Amount", 6000);

	    /** Resource Nodes */
	    // User Tom
	    Node tom = graphDb.createNode();
	    tom.setProperty(Constants.CLASS_KEY, NODE_CLASS_USER);
	    tom.setProperty(Constants.ID_KEY, "Tom");
	    // User John
	    Node john = graphDb.createNode();
	    john.setProperty(Constants.CLASS_KEY, NODE_CLASS_USER);
	    john.setProperty(Constants.ID_KEY, "John");
	    // GLAccount GLExpense
	    Node gLAExpense = graphDb.createNode();
	    gLAExpense.setProperty(Constants.CLASS_KEY, NODE_CLASS_GLACCOUNT);
	    gLAExpense.setProperty(Constants.ID_KEY, "Expense");
	    // GLAccount GLRevenue
	    Node gLARevenue = graphDb.createNode();
	    gLARevenue.setProperty(Constants.CLASS_KEY, NODE_CLASS_GLACCOUNT);
	    gLARevenue.setProperty(Constants.ID_KEY, "Revenue");

	    /** Edges */
	    s001.createRelationshipTo(q001, RelTypes.basedOn);
	    i001.createRelationshipTo(s001, RelTypes.bills);
	    invItem001.createRelationshipTo(i001, RelTypes.causedBy);
	    invItem002.createRelationshipTo(i001, RelTypes.causedBy);
	    invItem001.createRelationshipTo(gLARevenue, RelTypes.postedOn);
	    invItem002.createRelationshipTo(gLAExpense, RelTypes.postedOn);
	    q001.createRelationshipTo(tom, RelTypes.sentBy);
	    s001.createRelationshipTo(john, RelTypes.processedBy);
	    i001.createRelationshipTo(john, RelTypes.createdBy);

	    tom.createRelationshipTo(john, RelTypes.hasSupervisor);
	    q002.createRelationshipTo(tom, RelTypes.sentBy);
	    s002.createRelationshipTo(tom, RelTypes.processedBy);
	    s002.createRelationshipTo(q002, RelTypes.basedOn);
	    i002.createRelationshipTo(s002, RelTypes.bills);
	    i003.createRelationshipTo(s002, RelTypes.bills);
	    i002.createRelationshipTo(john, RelTypes.createdBy);
	    i003.createRelationshipTo(john, RelTypes.createdBy);
	    invItem003.createRelationshipTo(i002, RelTypes.causedBy);
	    invItem004.createRelationshipTo(i002, RelTypes.causedBy);
	    invItem005.createRelationshipTo(i003, RelTypes.causedBy);
	    invItem003.createRelationshipTo(gLAExpense, RelTypes.postedOn);
	    invItem004.createRelationshipTo(gLARevenue, RelTypes.postedOn);
	    invItem005.createRelationshipTo(gLARevenue, RelTypes.postedOn);

	    tx.success();
	} catch (Exception e) {
	    tx.failure();
	} finally {
	    tx.finish();
	}
    }

    @After
    public void destroyTestDatabase() {
	graphDb.shutdown();
    }
}
