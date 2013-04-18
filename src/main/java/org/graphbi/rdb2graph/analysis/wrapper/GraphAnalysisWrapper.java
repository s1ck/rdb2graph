package org.graphbi.rdb2graph.analysis.wrapper;

import java.util.Map;
import java.util.Set;

import org.graphbi.rdb2graph.util.config.NodeSuperClass;
import org.graphbi.rdb2graph.util.wrapper.GraphWrapper;

public interface GraphAnalysisWrapper extends GraphWrapper {
    
    public String getNodeClass(Long nodeId);
    
    public Set<Long> getNodesByClass(Map<String, NodeSuperClass> typeClassMap,
	    NodeSuperClass nodeClass);

    public Set<Long> getIncidentEdges(Long nodeId);

    public Set<Long> getAdjacentNodes(Long edgeId);

}
