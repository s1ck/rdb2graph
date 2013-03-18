package org.graphbi.rdb2graph.util;

public class DataSourceInfo {

    private final String type;

    private final String host;

    private final Integer port;

    private final String database;

    private final String user;

    private final String password;

    public DataSourceInfo(String type, String host, Integer port,
	    String database, String user, String password) {
	super();
	if (type == null || type.length() == 0) {
	    throw new IllegalArgumentException("type is null or empty");
	}
	if (host == null || host.length() == 0) {
	    throw new IllegalArgumentException("host is null or empty");
	}
	if (database == null || database.length() == 0) {
	    throw new IllegalArgumentException("database is null or empty");
	}
	if (port == null) {
	    throw new IllegalArgumentException("port is null");
	}
	if (user == null || user.length() == 0) {
	    throw new IllegalArgumentException("user is null or empty");
	}
	if (password == null || password.length() == 0) {
	    throw new IllegalArgumentException("password is null or empty");
	}
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

    public Integer getPort() {
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
