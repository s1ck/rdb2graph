package org.graphbi.rdb2graph.util.config;

public class Constants {
    /**
     * Key for node and edge identifiers.
     */
    public static final String ID_KEY = "__id__";
    /**
     * Key for node and edge type / class.
     */
    public static final String CLASS_KEY = "__class__";
    /**
     * Key for node's superclass (document / resource)
     */
    public static final String NODE_SUPER_CLASS_KEY = "__superclass__";
    /**
     * Key to track the source (e.g. the relational database) of a node / edge.
     */
    public static final String SOURCE_KEY = "__source__";
    /**
     * Key for row's primary key attribute value.
     */
    public static final String NODE_PK_KEY = "__pk__";
    /**
     * Key for the node's corresponding opgraph's position in the sorted.
     * collection of all opgraphs.
     */
    public static final String OPGRAPH_SORT_INDEX = "__opgraph_sortidx__";
    /**
     * Key for the nodes' corresponding opgraph's node count.
     */
    public static final String OPGRAPH_NODE_COUNT = "__opgraph_nodecnt__";
    /**
     * Key for the nodes' corresponding opgraph's edge count.
     */
    public static final String OPGRAPH_EDGE_COUNT = "__opgraph_edgecnt__";
}
