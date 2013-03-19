package org.graphbi.rdb2graph.util;

import javax.sql.DataSource;

import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.log4j.Logger;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

public class RDBPlatformFactory {

    private static Logger log = Logger.getLogger(RDBPlatformFactory.class);

    /**
     * 
     * TODO: Using a JDBC connection string would be a nicer generic solution,
     * but the platform doesn't create a datasource internally
     * 
     * @param dataSourceInfo
     * @return
     */
    public static Platform getInstance(DataSourceInfo dataSourceInfo) {
	DataSource ds = null;
	Platform p = null;
	if ("mysql".equals(dataSourceInfo.getType())) {
	    MysqlDataSource mysqlDs = new MysqlDataSource();
	    mysqlDs.setServerName(dataSourceInfo.getHost());
	    mysqlDs.setPort(dataSourceInfo.getPort());
	    mysqlDs.setUser(dataSourceInfo.getUser());
	    mysqlDs.setPassword(dataSourceInfo.getPassword());
	    mysqlDs.setDatabaseName(dataSourceInfo.getDatabase());
	    ds = (DataSource) mysqlDs;
	    p = PlatformFactory.createNewPlatformInstance(ds);
	} else if ("mssql".equals(dataSourceInfo.getType())) {
	    SQLServerDataSource sqlDS = new SQLServerDataSource();
	    sqlDS.setServerName(dataSourceInfo.getHost());
	    sqlDS.setPortNumber(dataSourceInfo.getPort());
	    sqlDS.setUser(dataSourceInfo.getUser());
	    sqlDS.setPassword(dataSourceInfo.getPassword());
	    sqlDS.setDatabaseName(dataSourceInfo.getDatabase());
	    ds = (DataSource) sqlDS;
	    p = PlatformFactory.createNewPlatformInstance(ds);
	    p.setDelimitedIdentifierModeOn(true);
	} else {
	    throw new IllegalArgumentException(
		    "Only MySQL and MSSQL are currently supported");
	}
	log.info(String.format("Initialized %s platform",
		dataSourceInfo.getType()));
	return p;
    }

}
