package org.graphbi.rdb2graph.util.graph;

public interface Graph {
	/**
	 * Returns the graph database instance's name.
	 * 
	 * @return Graph database instance's name.
	 */
	String getName();

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
}
