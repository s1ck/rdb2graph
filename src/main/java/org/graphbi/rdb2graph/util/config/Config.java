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
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	    log.error(e);
	} catch (IOException e) {
	    log.error(e);
	}
    }

    private void parseDatasource() {
	relationalStore = new DataSourceInfo(
		props.getProperty("datasource.type"),
		props.getProperty("datasource.host"), Integer.parseInt(props
			.getProperty("datasource.port")),
		props.getProperty("datasource.db"),
		props.getProperty("datasource.user"),
		props.getProperty("datasource.password"),
		Boolean.parseBoolean(props.getProperty("datasource.useSchema",
			"false")), Boolean.parseBoolean(props.getProperty(
			"datasource.useDelimiter", "false")));
    }

    private void parseGraphStore() {
	graphStore = new DataSinkInfo(props.getProperty("datasink.type"),
		props.getProperty("datasink.path"), Boolean.parseBoolean(props
			.getProperty("datasink.drop", "false")));
    }

    public DataSourceInfo getRelationalStore() {
	return this.relationalStore;
    }

    public DataSinkInfo getGraphStore() {
	return this.graphStore;
    }

    public DataSinkInfo getOpGraphStore() {
	return this.opGraphStore;
    }
}
