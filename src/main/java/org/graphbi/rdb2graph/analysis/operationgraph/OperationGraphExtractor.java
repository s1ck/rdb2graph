package org.graphbi.rdb2graph.analysis.operationgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.lang.time.StopWatch;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.apache.log4j.Logger;
import org.graphbi.rdb2graph.analysis.wrapper.GraphAnalysisWrapper;
import org.graphbi.rdb2graph.util.config.NodeSuperClass;

/**
 * Extracts and analyzes case graphs inside a given graph.
 * 
 * @author s1ck
 * 
 */
public class OperationGraphExtractor {
    private static Logger log = Logger.getLogger(OperationGraphExtractor.class);

    private final GraphAnalysisWrapper graphDB;
    private final Database relationalDB;

    /*
     * Maps the type of a node to a class (Resource / Document)
     */
    private Map<String, NodeSuperClass> nodeTypeClassMap;

    public OperationGraphExtractor(Database relationalDB,
	    GraphAnalysisWrapper graphWrapper) {
	if (relationalDB == null) {
	    throw new IllegalArgumentException(
		    "relationalModel must not be null.");
	}
	if (graphWrapper == null) {
	    throw new IllegalArgumentException("graphWrapper must not be null.");
	}
	this.relationalDB = relationalDB;
	this.graphDB = graphWrapper;
	this.nodeTypeClassMap = new HashMap<String, NodeSuperClass>();
    }

    private void init() {
	// read the class (document / resource) of each node class for faster
	// lookup during extraction
	for (Table t : relationalDB.getTables()) {
	    if (t.getDescription().toLowerCase().equals("r")) {
		nodeTypeClassMap.put(t.getNodeClass(), NodeSuperClass.RESOURCE);
	    } else if (t.getDescription().toLowerCase().equals("d")) {
		nodeTypeClassMap.put(t.getNodeClass(), NodeSuperClass.DOCUMENT);
	    }
	}
    }

    public List<OperationGraph> extract() {
	log.info("Extracting operation graphs.");
	StopWatch sw = new StopWatch();
	sw.start();
	init();
	// Operation graphs
	List<OperationGraph> opGraphs = new ArrayList<OperationGraph>();
	// candidate set of documents
	Set<Long> candidates = graphDB.getNodesByClass(nodeTypeClassMap,
		NodeSuperClass.DOCUMENT);
	log.info(String.format("Starting analysis for %d document nodes",
		candidates.size()));

	// V_C
	Queue<Long> globalCandidatesQueue = new LinkedList<Long>(candidates);
	// V_c
	Queue<Long> operationCandidatesQueue = null;
	// v_s
	Long discoveryStartNode = null;
	// v_c
	Long discoveryNode = null;
	// G_o
	OperationGraph opGraph = null;
	// E_c
	Set<Long> incidentEdges = null;
	// set of v_n
	Set<Long> nextCandidateSet = null;

	while ((discoveryStartNode = globalCandidatesQueue.peek()) != null) {
	    // create new operation graph
	    opGraph = new OperationGraph();
	    // and start with a unexplored node
	    opGraph.addNode(discoveryStartNode);
	    operationCandidatesQueue = new LinkedList<Long>();
	    operationCandidatesQueue.add(discoveryStartNode);
	    while ((discoveryNode = operationCandidatesQueue.peek()) != null) {
		globalCandidatesQueue.remove(discoveryNode);
		operationCandidatesQueue.remove(discoveryNode);
		// get incident edges of discovery node
		incidentEdges = graphDB.getIncidentEdges(discoveryNode);
		// remove all edges already stored in the operation graph
		incidentEdges.removeAll(opGraph.getEdges());
		for (Long edgeId : incidentEdges) {
		    nextCandidateSet = graphDB.getAdjacentNodes(edgeId);
		    nextCandidateSet.remove(discoveryNode);
		    // should be one left
		    for (Long nextCandidate : nextCandidateSet) {
			if (!opGraph.getNodes().contains(nextCandidate)) {
			    opGraph.addNode(nextCandidate);
			    String nodeClass = graphDB
				    .getNodeClass(nextCandidate);
			    if (nodeClass != null
				    && nodeTypeClassMap.containsKey(nodeClass)
				    && nodeTypeClassMap.get(nodeClass).equals(
					    NodeSuperClass.DOCUMENT)) {
				operationCandidatesQueue.add(nextCandidate);
			    }
			}
		    }
		    opGraph.addEdge(edgeId);
		}
	    }
	    opGraphs.add(opGraph);
	}
	sw.stop();
	log.info(String.format("Done. Found %d operation graphs. Took %s",
		opGraphs.size(), sw));
	return opGraphs;
    }
}
