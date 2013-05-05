package org.graphbi.rdb2graph.util.graph.impl.analyzer;

import org.graphbi.rdb2graph.analysis.documentgraph.analyzer.DocGraphMeasureFunction;
import org.graphbi.rdb2graph.util.graph.ReadOnlyGraph;
import org.graphbi.rdb2graph.util.graph.impl.Neo4jGraph;
import org.neo4j.graphdb.GraphDatabaseService;

public abstract class NeoDocGraphMeasureFunction<T> implements DocGraphMeasureFunction<T> {

    protected final GraphDatabaseService graph;

    public NeoDocGraphMeasureFunction(ReadOnlyGraph graph) {
	if (!(graph instanceof Neo4jGraph)) {
	    throw new IllegalArgumentException();
	}
	this.graph = ((Neo4jGraph) graph).getGraphDB();
    }

    public NeoDocGraphMeasureFunction(GraphDatabaseService graph) {
	this.graph = graph;
    }
}
