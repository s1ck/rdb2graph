package org.graphbi.rdb2graph.util.graph;

import java.util.Map;

public interface ReadWriteGraph extends ReadOnlyGraph {
    /**
     * Creates a node in the graph with the given properties.
     * 
     * @param properties
     *            Node's properties.
     * @return The node's system specific id if it has been created else null.
     */
    Long createNode(final Map<String, Object> properties);

    /**
     * Creates a relationship based on the system specific id value.
     * 
     * @param sourceID
     *            System specific source node id.
     * @param targetID
     *            System specifig target node id.
     * @param relType
     *            Relationship type.
     * @return The relationship's system specific id if it has been created else
     *         null.
     */
    Long createRelationship(final Long sourceID, final Long targetID,
	    final String relType);

    /**
     * Creates a relationship and it's properties based on the system specific
     * id value.
     * 
     * @param sourceID
     *            System specific source node id.
     * @param targetID
     *            System specifig target node id.
     * @param relType
     *            Relationship type.
     * @return The relationship's system specific id if it has been created else
     *         null.
     */
    Long createRelationship(final Long sourceID, final Long targetID,
	    final String relType, Map<String, Object> properties);

    /**
     * Creates a relationship based on the rdb2graph based ID-property value.
     * 
     * @param sourceID
     *            Source node's rdb2graph ID value.
     * @param targetID
     *            Target node's rdb2graph ID value.
     * @param relType
     *            Relationship type.
     * @return The relationship's system specific id if it has been created else
     *         null.
     */
    Long createRelationship(final String sourceID, final String targetID,
	    final String relType);

    /**
     * Creates a relationship and it's properties based on the rdb2graph based
     * ID-property value.
     * 
     * @param sourceID
     *            Source node's rdb2graph ID value.
     * @param targetID
     *            Target node's rdb2graph ID value.
     * @param relType
     *            Relationship type.
     * @param properties
     *            Relationship's properties.
     * @return The relationship's system specific id if it has been created else
     *         null.
     */
    Long createRelationship(final String sourceID, final String targetID,
	    final String relType, final Map<String, Object> properties);
}
