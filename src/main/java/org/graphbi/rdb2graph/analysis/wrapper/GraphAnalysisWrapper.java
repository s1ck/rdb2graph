package org.graphbi.rdb2graph.analysis.wrapper;

import java.util.Map;
import java.util.Set;

import org.graphbi.rdb2graph.util.config.NodeSuperClass;
import org.graphbi.rdb2graph.util.wrapper.GraphWrapper;

public interface GraphAnalysisWrapper extends GraphWrapper {
    /**
     * Returns the node's class (if the property has been set) else null.
     * 
     * @param nodeId
     * @return The node's class or null if it's not defined.
     */
    public String getNodeClass(Long nodeId);

    /**
     * Returns a set of nodeId's which belong to a given superclass
     * 
     * @param typeClassMap
     * @param nodeClass
     * @return
     */
    public Set<Long> getNodesBySuperClass(
	    Map<String, NodeSuperClass> typeClassMap, NodeSuperClass nodeClass);

    /**
     * Returns all nodes connected to the given edge.
     * 
     * @param edgeId
     *            The edge's id
     * @return A set of node ids.
     */
    public Set<Long> getIncidentNodes(Long edgeId);

    /**
     * Returns all adjacent nodes of the given node regardless of the edge's
     * direction.
     * 
     * @param nodeId
     * @return A set of adjacent nodes of nodeId
     */
    public Set<Long> getAdjacentNodes(Long nodeId);

    /**
     * Returns all incident edges of the given node.
     * 
     * @param nodeId
     * @return A set of incident edges of nodeId
     */
    public Set<Long> getIncidentEdges(Long nodeId);

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
    public Set<Long> getIncidentEdges(Long nodeId, boolean skipInstance);
}
