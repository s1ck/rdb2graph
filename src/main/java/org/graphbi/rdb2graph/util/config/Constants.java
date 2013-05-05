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
     * Key for the docgraphs's identifier.
     */
    public static final String DOCGRAPH_ID = "__docgraph_id__";
    /**
     * Key for the node's corresponding docgraph's position in the sorted.
     * collection of all docgraphs.
     */
    public static final String DOCGRAPH_SORT_INDEX = "__docgraph_sortidx__";
    /**
     * Key for the nodes' corresponding docgraph's node count.
     */
    public static final String DOCGRAPH_NODE_COUNT = "__docgraph_nodecnt__";
    /**
     * Key for the nodes' corresponding docgraph's edge count.
     */
    public static final String DOCGRAPH_EDGE_COUNT = "__docgraph_edgecnt__";
}
