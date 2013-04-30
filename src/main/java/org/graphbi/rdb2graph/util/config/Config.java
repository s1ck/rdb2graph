package org.graphbi.rdb2graph.util.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Properties;

import org.apache.log4j.Logger;

public class Config {
    private static Logger log = Logger.getLogger(Config.class);

    /**
     * Used to concatenate schema and table id "useSchema" is set to true
     */
    public static final String DELIMITER_CONCAT = ".";

    private Properties props;
    private final File file;

    private DataSourceInfo relationalStore;
    private DataSinkInfo graphStore;
    private DataSinkInfo opGraphStore;

    public Config(String cfg) {
	if (cfg == null || cfg.length() == 0) {
	    throw new IllegalArgumentException("config file is null or missing");
	}
	File f = new File(cfg);
	if (!f.exists()) {
	    throw new InvalidParameterException(String.format(
		    "File %s does not exist", cfg));
	}
	this.file = f;
	this.props = new Properties();
    }

    public void parse() {
	log.info(String.format("Parsing %s", file.getName()));
	try {
	    props.load(new FileInputStream(file));

	    parseDatasource();
	    parseGraphStore();
	    // parse optional opgraphstore
	    if (props.getProperty("opgraphstore.type") != null) {
		parseOpGraphStore();
	    } else {
		opGraphStore = null;
	    }
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	    log.error(e);
	} catch (IOException e) {
	    log.error(e);
	}
    }

    /**
     * Returns connection information about the relational store.
     * 
     * @return Information about the relational database.
     */
    public DataSourceInfo getRelationalStore() {
	return this.relationalStore;
    }

    /**
     * Returns connection information about the graph store.
     * 
     * @return Information about the graph database.
     */
    public DataSinkInfo getGraphStore() {
	return this.graphStore;
    }

    /**
     * Returns connection infomation about the operation graph store or null if
     * none was configured.
     * 
     * @return Information about the opgraph database or null.
     */
    public DataSinkInfo getOpGraphStore() {
	return this.opGraphStore;
    }

    private void parseDatasource() {
	relationalStore = new DataSourceInfo(
		props.getProperty("relstore.type"),
		props.getProperty("relstore.host"), Integer.parseInt(props
			.getProperty("relstore.port")),
		props.getProperty("relstore.db"),
		props.getProperty("relstore.user"),
		props.getProperty("relstore.password"),
		Boolean.parseBoolean(props.getProperty("relstore.useSchema",
			"false")), Boolean.parseBoolean(props.getProperty(
			"relstore.useDelimiter", "false")));
    }

    private void parseGraphStore() {
	graphStore = new DataSinkInfo(props.getProperty("graphstore.type"),
		props.getProperty("graphstore.path"),
		Boolean.parseBoolean(props.getProperty("graphstore.drop",
			"false")), Boolean.parseBoolean(props.getProperty(
			"graphstore.useReferenceNode", "true")));
    }

    private void parseOpGraphStore() {
	opGraphStore = new DataSinkInfo(props.getProperty("opgraphstore.type"),
		props.getProperty("opgraphstore.path"),
		Boolean.parseBoolean(props.getProperty("opgraphstore.drop",
			"false")), Boolean.parseBoolean(props.getProperty(
			"graphstore.useReferenceNode", "true")));
    }
}
