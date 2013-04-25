package org.graphbi.rdb2graph.analysis.operationgraph;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.graphbi.rdb2graph.util.config.Constants;
import org.graphbi.rdb2graph.util.graph.ReadOnlyGraph;
import org.graphbi.rdb2graph.util.graph.ReadWriteGraph;

/**
 * Copies extracted operation graphs into a dedicated graph database for better
 * analysis.
 * 
 * @author Martin Junghanns
 * 
 */
public class OperationGraphDuplicator {
    private static final Logger log = Logger
	    .getLogger(OperationGraphDuplicator.class);

    private final ReadOnlyGraph fromGraphDB;
    private final ReadWriteGraph toGraphDB;

    public OperationGraphDuplicator(ReadOnlyGraph fromGraphDB,
	    ReadWriteGraph toGraphDB) {
	this.fromGraphDB = fromGraphDB;
	this.toGraphDB = toGraphDB;
    }

    public void duplicate(List<OperationGraph> opGraphs) {
	log.info(String.format("Copying %d operation graphs", opGraphs.size()));
	StopWatch sw = new StopWatch();
	sw.start();

	Map<String, Long> nodeIdx = new HashMap<String, Long>();
	Map<String, Object> properties = null;
	Long newNodeId = null;
	String edgeType = null;
	Long[] incidentNodes = null;
	String fromNode = null;
	String toNode = null;

	// sort opgraphs to store index position at nodes
	// ascending
	// Collections.sort(opGraphs);
	// descending
	Collections.sort(opGraphs, new Comparator<OperationGraph>() {
	    @Override
	    public int compare(OperationGraph o1, OperationGraph o2) {
		return o1.compareTo(o2) * -1;
	    }
	});

	int opGraph_idx = 0;

	for (OperationGraph opGraph : opGraphs) {
	    nodeIdx.clear();
	    toGraphDB.beginTransaction();
	    for (Long nodeId : opGraph.getNodes()) {
		// read properties from source system
		properties = fromGraphDB.getNodeProperties(nodeId);
		// add meta data about the opgraph
		properties.put(Constants.OPGRAPH_SORT_INDEX, opGraph_idx);
		properties.put(Constants.OPGRAPH_NODE_COUNT,
			opGraph.getNodeCount());
		properties.put(Constants.OPGRAPH_EDGE_COUNT,
			opGraph.getEdgeCount());

		// and write them to target system
		newNodeId = toGraphDB.createNode(properties, true);
		nodeIdx.put((String) properties.get(Constants.ID_KEY),
			newNodeId);
	    }
	    for (Long edgeId : opGraph.getEdges()) {
		// read properties and type from source system
		properties = fromGraphDB.getEdgeProperties(edgeId);
		edgeType = fromGraphDB.getEdgeType(edgeId);
		incidentNodes = fromGraphDB.getIncidentNodes(edgeId);
		fromNode = (String) fromGraphDB.getNodeProperties(
			incidentNodes[0]).get(Constants.ID_KEY);
		toNode = (String) fromGraphDB.getNodeProperties(
			incidentNodes[1]).get(Constants.ID_KEY);
		// create new relationship
		toGraphDB.createRelationship(nodeIdx.get(fromNode),
			nodeIdx.get(toNode), edgeType, properties);
	    }
	    toGraphDB.successTransaction();
	    toGraphDB.finishTransaction();
	    opGraph_idx++;
	}
	sw.stop();
	log.info(String.format("Done. Took %s", sw));
    }
}
