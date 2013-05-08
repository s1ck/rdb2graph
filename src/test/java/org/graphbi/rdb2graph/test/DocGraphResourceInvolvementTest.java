package org.graphbi.rdb2graph.test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.graphbi.rdb2graph.RDB2Graph;
import org.graphbi.rdb2graph.analysis.documentgraph.DocGraph;
import org.graphbi.rdb2graph.analysis.documentgraph.DocGraphDuplicator;
import org.graphbi.rdb2graph.analysis.documentgraph.DocGraphExtractor;
import org.graphbi.rdb2graph.analysis.documentgraph.analyzer.ResourceInvolvementExtractor;
import org.graphbi.rdb2graph.util.config.Config;
import org.graphbi.rdb2graph.util.graph.impl.Neo4jGraph;
import org.graphbi.rdb2graph.util.graph.impl.analyzer.NeoResourceInvolvementExtractor;
import org.junit.Assert;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.test.TestGraphDatabaseFactory;

public class DocGraphResourceInvolvementTest extends GraphBITest {

    @Test
    public void testResourceInvolvementExtraction() {
	// overwrite config
	cfg = new Config(RDB2Graph.class.getResource(
		"/config_no_reference.properties").getFile());
	cfg.parse();

	// extract document graphs
	Neo4jGraph fromGraph = new Neo4jGraph(graphDb);
	GraphDatabaseService toGraphDB = new TestGraphDatabaseFactory()
		.newImpermanentDatabaseBuilder().newGraphDatabase();
	Neo4jGraph toGraph = new Neo4jGraph(toGraphDB);

	List<DocGraph> docGraphs = new DocGraphExtractor(fromGraph,
		nodeClassSuperClassMap).extract();

	// and move them to a new db
	DocGraphDuplicator mover = new DocGraphDuplicator(cfg, fromGraph,
		toGraph);
	mover.duplicate(docGraphs);
	// and extract them again to get the correct ids
	docGraphs = new DocGraphExtractor(toGraph, nodeClassSuperClassMap)
		.extract();

	// start extracting involvements from new db
	Set<String> interestingResources = new HashSet<String>();
	interestingResources.add(NODE_CLASS_USER);
	Set<String> interestingDocuments = new HashSet<String>();
	interestingDocuments.add(NODE_CLASS_INVOICE);
	ResourceInvolvementExtractor resourceExtractor = new NeoResourceInvolvementExtractor(
		toGraphDB);
	Map<String, Set<Long>> associations = resourceExtractor.extract(
		docGraphs, null, interestingResources, interestingDocuments);
	
	Assert.assertNotNull(associations);
	Assert.assertEquals(5, associations.size());
	

	String pattern;

	pattern = "John<-createdBy-Invoice-bills->SalesOrder<-bills-Invoice";
	Assert.assertTrue(associations.containsKey(pattern));
	Assert.assertEquals(1, associations.get(pattern).size()); // 1

	pattern = "John<-createdBy-Invoice";
	Assert.assertTrue(associations.containsKey(pattern));
	Assert.assertEquals(2, associations.get(pattern).size()); // 0,1

	pattern = "John<-processedBy-SalesOrder<-bills-Invoice";
	Assert.assertTrue(associations.containsKey(pattern));
	Assert.assertEquals(1, associations.get(pattern).size()); // 0

	pattern = "Tom<-processedBy-SalesOrder<-bills-Invoice";
	Assert.assertTrue(associations.containsKey(pattern));
	Assert.assertEquals(1, associations.get(pattern).size()); // 1

	pattern = "Tom<-sentBy-Quotation<-basedOn-SalesOrder<-bills-Invoice";
	Assert.assertTrue(associations.containsKey(pattern));
	Assert.assertEquals(2, associations.get(pattern).size()); // 0,1
    }
}
