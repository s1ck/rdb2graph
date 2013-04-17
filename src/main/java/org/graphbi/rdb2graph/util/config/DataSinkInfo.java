package org.graphbi.rdb2graph.util.config;

public class DataSinkInfo {

    private final String type;

    private final String path;

    private final Boolean drop;

    public DataSinkInfo(String type, String path, Boolean drop) {
	super();
	if (type == null || type.length() == 0) {
	    throw new IllegalArgumentException("type is null or empty");
	}
	if (path == null || path.length() == 0) {
	    throw new IllegalArgumentException("path is null or empty");
	}
	if (drop == null) {
	    throw new IllegalArgumentException("drop is null");
	}
	this.type = type;
	this.path = path;
	this.drop = drop;
    }

    public String getType() {
        return type;
    }

    public String getPath() {
        return path;
    }

    public Boolean getDrop() {
        return drop;
    }

    @Override
    public String toString() {
	return "DataSinkInfo [type=" + type + ", path=" + path + ", drop="
		+ drop + "]";
    }

}
