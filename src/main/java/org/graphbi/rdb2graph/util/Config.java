package org.graphbi.rdb2graph.util;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Config {
    private static Logger log = Logger.getLogger(Config.class);

    /**
     * Used to concatenate schema and table id "useSchema" is set to true
     */
    public static final String DELIMITER_CONCAT = ".";

    private final File file;

    private DataSourceInfo dataSourceInfo;
    private DataSinkInfo dataSinkInfo;

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
    }

    public void parse() {
	log.info(String.format("Parsing %s", file.getName()));
	try {
	    DocumentBuilderFactory dbFactory = DocumentBuilderFactory
		    .newInstance();
	    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

	    Document doc = dBuilder.parse(file);

	    parseDatasource(doc);
	    parseDatasink(doc);
	} catch (ParserConfigurationException e) {
	    e.printStackTrace();
	} catch (SAXException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    private void parseDatasource(Document doc) {
	NodeList datasourceList = doc.getElementsByTagName("datasource");
	int length = datasourceList.getLength();
	log.info(String.format("Parsing %d datasources", length));
	if (length > 1) {
	    log.warn("Multiple datasources are currently not supported!");
	}
	Node dataSourceNode = null;
	Element dataSourceElement = null;

	String type, host, user, password, database;
	Integer port;
	Boolean useSchema = false, useDelimiter = false;

	// for (int i = 0; i < length; i++) {
	dataSourceNode = datasourceList.item(0);
	if (dataSourceNode.getNodeType() == Node.ELEMENT_NODE) {
	    dataSourceElement = (Element) dataSourceNode;
	    type = dataSourceElement.getAttribute("type");
	    database = dataSourceElement.getAttribute("db");
	    host = dataSourceElement.getAttribute("host");
	    user = dataSourceElement.getAttribute("user");
	    password = dataSourceElement.getAttribute("password");
	    port = Integer.parseInt(dataSourceElement.getAttribute("port"));
	    if (dataSourceElement.hasAttribute("useSchema")) {
		useSchema = Boolean.parseBoolean(dataSourceElement
			.getAttribute("useSchema"));
	    }
	    if (dataSourceElement.hasAttribute("useDelimiter")) {
		useDelimiter = Boolean.parseBoolean(dataSourceElement
			.getAttribute("useDelimiter"));
	    }
	    dataSourceInfo = new DataSourceInfo(type, host, port, database,
		    user, password, useSchema, useDelimiter);
	}
	// }
    }

    private void parseDatasink(Document doc) {
	NodeList datasinkList = doc.getElementsByTagName("datasink");
	int length = datasinkList.getLength();
	log.info(String.format("Parsing %d datasinks", length));
	if (length > 1) {
	    log.warn("Multiple datasinks are currently not supported!");
	}
	Node dataSinkNode = null;
	Element dataSinkElement = null;

	String type, path;
	Boolean drop;

	dataSinkNode = datasinkList.item(0);
	if (dataSinkNode.getNodeType() == Node.ELEMENT_NODE) {
	    dataSinkElement = (Element) dataSinkNode;
	    type = dataSinkElement.getAttribute("type");
	    path = dataSinkElement.getAttribute("path");
	    drop = Boolean.parseBoolean(dataSinkElement.getAttribute("drop"));

	    dataSinkInfo = new DataSinkInfo(type, path, drop);
	}
    }

    public DataSourceInfo getDataSourceInfo() {
	return this.dataSourceInfo;
    }

    public DataSinkInfo getDataSinkInfo() {
	return this.dataSinkInfo;
    }
}
