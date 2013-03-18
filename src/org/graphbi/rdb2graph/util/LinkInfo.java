package org.graphbi.rdb2graph.util;

public class LinkInfo {

    private final LinkTableInfo linkTableInfo;

    private final String linkName;

    private final String fromTableName;

    private final String fromColumnName;

    private final String toTableName;

    private final String toColumnName;

    public LinkInfo(LinkTableInfo linkTableInfo, String linkName,
	    String fromTableName, String fromColumnName, String toTableName,
	    String toColumnName) {
	super();
	if (linkTableInfo == null) {
	    throw new IllegalArgumentException("linkTable is null");
	}
	if (linkName == null || linkName.length() == 0) {
	    throw new IllegalArgumentException("linkName is null or empty");
	}
	if (fromTableName == null || fromTableName.length() == 0) {
	    throw new IllegalArgumentException(
		    "fromTableName is null or empty");
	}
	if (fromColumnName == null || fromColumnName.length() == 0) {
	    throw new IllegalArgumentException(
		    "fromColumnName is null or empty");
	}
	if (toTableName == null || toTableName.length() == 0) {
	    throw new IllegalArgumentException("toTableName is null or empty");
	}
	if (toColumnName == null || toColumnName.length() == 0) {
	    throw new IllegalArgumentException(
		    "toColumnName is null or empty");
	}
	this.linkTableInfo = linkTableInfo;
	this.linkName = linkName;
	this.fromTableName = fromTableName;
	this.fromColumnName = fromColumnName;
	this.toTableName = toTableName;
	this.toColumnName = toColumnName;
    }

    public LinkTableInfo getLinkTableInfo() {
	return linkTableInfo;
    }

    public String getLinkName() {
	return linkName;
    }

    public String getFromTableName() {
	return fromTableName;
    }

    public String getFromColumnName() {
	return fromColumnName;
    }

    public String getToTableName() {
	return toTableName;
    }

    public String getToColumnName() {
	return toColumnName;
    }

    @Override
    public String toString() {
	return "LinkInfo [linkTableInfo=" + linkTableInfo + ", linkName="
		+ linkName + ", fromTableName=" + fromTableName
		+ ", fromColumnName=" + fromColumnName + ", toTableName="
		+ toTableName + ", toColumnName=" + toColumnName + "]";
    }
}
