package org.graphbi.rdb2graph.util;

public class DataSourceInfo {

    private final String type;

    private final String host;

    private final int port;

    private final String database;

    private final String user;

    private final String password;

    public DataSourceInfo(String type, String host, int port, String database,
	    String user, String password) {
	super();
	this.type = type;
	this.host = host;
	this.port = port;
	this.database = database;
	this.user = user;
	this.password = password;
    }

    public String getType() {
	return type;
    }

    public String getHost() {
	return host;
    }

    public int getPort() {
	return port;
    }

    public String getDatabase() {
	return database;
    }

    public String getUser() {
	return user;
    }

    public String getPassword() {
	return password;
    }

    @Override
    public String toString() {
	return "DataSource [type=" + type + ", host=" + host + ", port=" + port
		+ ", database=" + database + ", user=" + user + ", password="
		+ password + "]";
    }
}
