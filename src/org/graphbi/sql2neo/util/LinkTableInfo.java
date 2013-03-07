package org.graphbi.sql2neo.util;

public class LinkTableInfo {

    private final String linkTable;

    private final String linkName;

    private final String fk1;

    private final String fk2;

    public LinkTableInfo(final String linkTable, final String linkName,
	    final String fk1, final String fk2) {
	if (linkTable == null || linkTable.length() == 0) {
	    throw new IllegalArgumentException("linkName is null or missing");
	}
	if (linkName == null || linkName.length() == 0) {
	    throw new IllegalArgumentException("linkName is null or missing");
	}
	if (fk1 == null || fk1.length() == 0) {
	    throw new IllegalArgumentException("fk1 is null or missing");
	}
	if (fk2 == null || fk2.length() == 0) {
	    throw new IllegalArgumentException("fk2 is null or missing");
	}
	this.linkTable = linkTable;
	this.linkName = linkName;
	this.fk1 = fk1;
	this.fk2 = fk2;
    }

    public String getLinkTable() {
	return this.linkTable;
    }

    public String getLinkName() {
	return this.linkName;
    }

    public String getFk1() {
	return this.fk1;
    }

    public String getFk2() {
	return this.fk2;
    }

    @Override
    public String toString() {
	return "LinkTableInfo [linkTable=" + linkTable + ", linkName="
		+ linkName + ", fk1=" + fk1 + ", fk2=" + fk2 + "]";
    }
}
