package org.graphbi.rdb2graph.util.graph.impl.analyzer;

import org.graphbi.rdb2graph.analysis.documentgraph.analyzer.DocGraphFilterFunction;
import org.graphbi.rdb2graph.util.graph.ReadOnlyGraph;
import org.graphbi.rdb2graph.util.graph.impl.Neo4jGraph;
import org.neo4j.graphdb.GraphDatabaseService;

public abstract class NeoDocGraphFilterFunction implements DocGraphFilterFunction {
    
    protected final GraphDatabaseService graph;
    
    public NeoDocGraphFilterFunction(ReadOnlyGraph graph) {
	if (!(graph instanceof Neo4jGraph)) {
	    throw new IllegalArgumentException();
	}
	this.graph = ((Neo4jGraph)graph).getGraphDB();
    }
    
    public NeoDocGraphFilterFunction(GraphDatabaseService graph) {
	this.graph = graph;
    }
}
