package org.graphbi.rdb2graph.util.graph;

import java.util.Map;
import java.util.Set;

import org.graphbi.rdb2graph.util.config.NodeSuperClass;

public interface ReadOnlyGraph extends Graph {
    /**
     * Returns the node's class (if the property has been set) else null.
     * 
     * @param nodeId
     * @return The node's class or null if it's not defined.
     */
    String getNodeClass(Long nodeId);

    /**
     * Returns the properties of a given node or null if the node doesn't exist.
     * 
     * @param nodeId
     *            The system specific node id.
     * @return The node's properties or null if the node doesn't exist.
     */
    Map<String, Object> getNodeProperties(Long nodeId);

    /**
     * Returns the properties of a given edge or null if the edge doesn't exist.
     * 
     * @param edgeId
     *            The system specific edge id.
     * @return The edge's properties or null if the edge doesn't exist.
     */
    Map<String, Object> getEdgeProperties(Long edgeId);

    /**
     * Returns the type of the given edge.
     * 
     * @param edgeId
     *            The edge's system specific id.
     * @return The edge type or null if the edge doesn't exist.
     */
    String getEdgeType(Long edgeId);

    /**
     * Returns a set of nodeId's which belong to a given superclass
     * 
     * @param typeClassMap
     * @param nodeClass
     * @return
     */
    Set<Long> getNodesBySuperClass(Map<String, NodeSuperClass> typeClassMap,
	    NodeSuperClass nodeClass);

    /**
     * Returns all nodes connected to the given edge.
     * 
     * @param edgeId
     *            The edge's id
     * @return Edge information containing source node's id and target node's
     *         id.
     */
    Long[] getIncidentNodes(Long edgeId);

    /**
     * Returns all adjacent nodes of the given node regardless of the edge's
     * direction.
     * 
     * @param nodeId
     * @return A set of adjacent nodes of nodeId
     */
    Set<Long> getAdjacentNodes(Long nodeId);

    /**
     * Returns all incident edges of the given node.
     * 
     * @param nodeId
     * @return A set of incident edges of nodeId
     */
    Set<Long> getIncidentEdges(Long nodeId);

    /**
     * Returns all incident edges of the given node.
     * 
     * @param nodeId
     *            The node's id
     * @param skipInstance
     *            if the graph uses instance edges these can be skipped using
     *            this parameter.
     * @return A set of incident edges of nodeId
     */
    Set<Long> getIncidentEdges(Long nodeId, boolean skipInstance);
}
