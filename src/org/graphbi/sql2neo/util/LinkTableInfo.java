package org.graphbi.sql2neo.util;

import java.util.ArrayList;
import java.util.List;

public class LinkTableInfo {

    private final String name;

    private List<LinkInfo> links;

    public LinkTableInfo(final String name) {
	if (name == null || name.length() == 0) {
	    throw new IllegalArgumentException("linkName is null or missing");
	}
	this.name = name;
	this.links = new ArrayList<LinkInfo>();
    }

    public String getName() {
	return this.name;
    }

    public void addLinkInfo(LinkInfo linkInfo) {
	links.add(linkInfo);
    }

    public List<LinkInfo> getLinkInfos() {
	return links;
    }

    @Override
    public String toString() {
	return "LinkTableInfo [linkTableName=" + name + "]";
    }
}
