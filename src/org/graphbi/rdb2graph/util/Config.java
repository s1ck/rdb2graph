package org.graphbi.rdb2graph.util;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

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

    private final File file;

    private DataSourceInfo dataSource;
    private DataSinkInfo dataSink;
    private List<LinkTableInfo> linkTables;

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
	this.linkTables = new ArrayList<LinkTableInfo>();
    }

    public void parse() {
	log.info(String.format("Parsing %s", file.getName()));
	try {
	    DocumentBuilderFactory dbFactory = DocumentBuilderFactory
		    .newInstance();
	    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

	    Document doc = dBuilder.parse(file);

	    parseDatasource(doc);
	    parseLinkTableInfos(doc);
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

	    dataSource = new DataSourceInfo(type, host, port, database, user,
		    password);
	}
	// }
    }

    private void parseLinkTableInfos(Document doc) {
	NodeList linkTableList = doc.getElementsByTagName("linktable");
	log.info(String.format("Parsing %d linktables",
		linkTableList.getLength()));
	NodeList linkList = null;
	Node linkTableNode = null, linkNode = null;
	Element linkTableElement = null, linkElement = null;
	LinkTableInfo lt = null;
	String linkTableName = null, linkName = null;
	String fromTable = null, fromColumn = null;
	String toTable = null, toColumn = null;
	for (int i = 0; i < linkTableList.getLength(); i++) {
	    linkTableNode = linkTableList.item(i);

	    if (linkTableNode.getNodeType() == Node.ELEMENT_NODE) {
		linkTableElement = (Element) linkTableNode;
		linkTableName = linkTableElement.getAttribute("name");
		linkList = linkTableElement.getChildNodes();
		lt = new LinkTableInfo(linkTableName);
		for (int j = 0; j < linkList.getLength(); j++) {
		    linkNode = linkList.item(j);
		    if (linkNode.getNodeType() == Node.ELEMENT_NODE) {
			linkElement = (Element) linkNode;
			linkName = linkElement.getAttribute("name");
			fromTable = linkElement.getAttribute("fromTable");
			fromColumn = linkElement.getAttribute("fromColumn");
			toTable = linkElement.getAttribute("toTable");
			toColumn = linkElement.getAttribute("toColumn");
			lt.addLinkInfo(new LinkInfo(lt, linkName, fromTable,
				fromColumn, toTable, toColumn));
		    }
		}
	    }
	    linkTables.add(lt);
	}
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

	    dataSink = new DataSinkInfo(type, path, drop);
	}
    }

    public DataSourceInfo getDataSource() {
	return this.dataSource;
    }

    public DataSinkInfo getDataSink() {
	return this.dataSink;
    }

    public List<LinkTableInfo> getLinkTableInfos() {
	return this.linkTables;
    }
}
