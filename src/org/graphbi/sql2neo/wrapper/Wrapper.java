package org.graphbi.sql2neo.wrapper;

import java.util.Map;

public interface Wrapper {
    void beginTransaction();

    void successTransaction();

    void finishTransaction();

    void createNode(Map<String, Object> properties);

    void createRelationship(String sourceID, String targetID, String relType,
	    Map<String, Object> properties);

}
