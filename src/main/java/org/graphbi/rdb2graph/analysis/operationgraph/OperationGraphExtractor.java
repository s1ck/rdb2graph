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
import org.graphbi.rdb2graph.util.config.NodeSuperClass;
import org.graphbi.rdb2graph.util.graph.ReadOnlyGraph;

/**
 * Extracts and analyzes case graphs inside a given graph.
 * 
 * @author s1ck
 * 
 */
public class OperationGraphExtractor {
    private static Logger log = Logger.getLogger(OperationGraphExtractor.class);

    private final ReadOnlyGraph graphDB;
    private final Database relationalDB;

    /*
     * Maps the type of a node to a class (Resource / Document)
     */
    private final Map<String, NodeSuperClass> nodeClassSuperClassMap;

    public OperationGraphExtractor(ReadOnlyGraph graphWrapper,
	    Map<String, NodeSuperClass> nodeClassSuperClassMap) {
	if (graphWrapper == null) {
	    throw new IllegalArgumentException("graphWrapper must not be null.");
	}
	this.graphDB = graphWrapper;
	this.relationalDB = null;
	this.nodeClassSuperClassMap = nodeClassSuperClassMap;
    }

    public OperationGraphExtractor(ReadOnlyGraph graphWrapper,
	    Database relationalDB) {
	if (relationalDB == null) {
	    throw new IllegalArgumentException(
		    "relationalModel must not be null.");
	}
	if (graphWrapper == null) {
	    throw new IllegalArgumentException("graphWrapper must not be null.");
	}
	this.relationalDB = relationalDB;
	this.graphDB = graphWrapper;
	this.nodeClassSuperClassMap = initNodeClassSuperClassMap();
    }

    private Map<String, NodeSuperClass> initNodeClassSuperClassMap() {
	// read the class (document / resource) of each node class for faster
	// lookup during extraction
	Map<String, NodeSuperClass> nodeClassSuperClassMap = new HashMap<String, NodeSuperClass>();
	for (Table t : relationalDB.getTables()) {
	    if (t.getDescription().toLowerCase().equals("r")) {
		nodeClassSuperClassMap.put(t.getNodeClass(),
			NodeSuperClass.RESOURCE);
	    } else if (t.getDescription().toLowerCase().equals("d")) {
		nodeClassSuperClassMap.put(t.getNodeClass(),
			NodeSuperClass.DOCUMENT);
	    }
	}
	return nodeClassSuperClassMap;
    }

    public List<OperationGraph> extract() {
	log.info("Extracting operation graphs.");
	StopWatch sw = new StopWatch();
	sw.start();

	// Operation graphs
	List<OperationGraph> opGraphs = new ArrayList<OperationGraph>();
	// candidate set of documents
	Set<Long> candidates = graphDB.getNodesBySuperClass(
		nodeClassSuperClassMap, NodeSuperClass.DOCUMENT);
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
	// Array of v_n candidates
	Long[] nextCandidates = null;
	// v_n
	Long nextCandidate = null;

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
		    nextCandidates = graphDB.getIncidentNodes(edgeId);
		    // assuming that there are only two nodes connected to that
		    // edge
		    nextCandidate = (nextCandidates[0] == discoveryNode) ? nextCandidates[1]
			    : nextCandidates[0];
		    if (!opGraph.getNodes().contains(nextCandidate)) {
			opGraph.addNode(nextCandidate);
			String nodeClass = graphDB.getNodeClass(nextCandidate);
			if (nodeClass != null
				&& nodeClassSuperClassMap
					.containsKey(nodeClass)
				&& nodeClassSuperClassMap.get(nodeClass)
					.equals(NodeSuperClass.DOCUMENT)) {
			    operationCandidatesQueue.add(nextCandidate);
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
