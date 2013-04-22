package org.graphbi.rdb2graph.analysis.operationgraph;

import java.util.List;

import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.graphbi.rdb2graph.util.graph.ReadOnlyGraph;
import org.graphbi.rdb2graph.util.graph.ReadWriteGraph;

/**
 * Moves extracted operation graphs into a dedicated graph database for better
 * analysis.
 * 
 * @author Martin Junghanns
 * 
 */
public class OperationGraphMover {
    private static final Logger log = Logger
	    .getLogger(OperationGraphMover.class);

    private final ReadOnlyGraph fromGraphDB;

    private final ReadWriteGraph toGraphDB;

    public OperationGraphMover(ReadOnlyGraph fromGraphDB,
	    ReadWriteGraph toGraphDB) {
	this.fromGraphDB = fromGraphDB;
	this.toGraphDB = toGraphDB;
    }

    public void move(List<OperationGraph> opGraphs) {
	log.info(String.format("Moving %d operation graphs", opGraphs.size()));
	StopWatch sw = new StopWatch();
	sw.start();
	

	sw.stop();
	log.info(String.format("Done. Took %s", sw));
    }

}
