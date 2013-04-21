package org.graphbi.rdb2graph.util.graph;

import java.util.Map;

public interface ReadWriteGraph extends ReadOnlyGraph {
    /**
     * Creates a node in the graph with the given properties.
     * 
     * @param properties
     * @return True, if the node has been created.
     */
    boolean createNode(final Map<String, Object> properties);
    /**
     * Creates a relationship (edge) between two existing nodes in the graph.
     * 
     * @param sourceID
     * @param targetID
     * @param relType
     * @return True, if the edge has been created.
     */
    boolean createRelationship(final String sourceID, final String targetID,
	    final String relType);
    /**
     * Creates a relationship (edge) between two existing nodes in the graph.
     * 
     * @param sourceID
     * @param targetID
     * @param relType
     * @param properties
     * @return True, if the edge has been created.
     */
    boolean createRelationship(final String sourceID, final String targetID,
	    final String relType, final Map<String, Object> properties);
}
