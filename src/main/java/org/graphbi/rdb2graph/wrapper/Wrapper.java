package org.graphbi.rdb2graph.wrapper;

import java.util.Map;

public interface Wrapper {
    /**
     * Begins a transaction.
     * 
     * Can be used in batch processing scenarios.
     */
    void beginTransaction();

    /**
     * Commits a transaction.
     */
    void successTransaction();

    /**
     * Finishs a transaction.
     */
    void finishTransaction();

    /**
     * Rollback of transaction.
     */
    void rollbackTransaction();

    void createNode(final Map<String, Object> properties);

    void createRelationship(final String sourceID, final String targetID,
	    final String relType);

    void createRelationship(final String sourceID, final String targetID,
	    final String relType, final Map<String, Object> properties);

}
