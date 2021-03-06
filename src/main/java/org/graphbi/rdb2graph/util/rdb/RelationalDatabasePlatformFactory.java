package org.graphbi.rdb2graph.util.rdb;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.log4j.Logger;
import org.graphbi.rdb2graph.util.config.DataSourceInfo;

import javax.sql.DataSource;

/**
 * Creates a Platform instance based on the configured data source.
 *
 * @author Martin Junghanns
 */
public class RelationalDatabasePlatformFactory {

  private static Logger log =
    Logger.getLogger(RelationalDatabasePlatformFactory.class);

  /**
   * TODO: Using a JDBC connection string would be a nicer generic solution,
   * but the platform doesn't create a datasource internally
   *
   * @param dataSourceInfo
   * @return
   */
  public static Platform getInstance(DataSourceInfo dataSourceInfo) {
    DataSource ds;
    Platform p;
    if ("mysql".equals(dataSourceInfo.getType())) {
      MysqlDataSource mysqlDs = new MysqlDataSource();
      mysqlDs.setServerName(dataSourceInfo.getHost());
      mysqlDs.setPort(dataSourceInfo.getPort());
      mysqlDs.setUser(dataSourceInfo.getUser());
      mysqlDs.setPassword(dataSourceInfo.getPassword());
      mysqlDs.setDatabaseName(dataSourceInfo.getDatabase());
      ds = mysqlDs;
      p = PlatformFactory.createNewPlatformInstance(ds);
    } else {
      throw new IllegalArgumentException(
        "Only MySQL and MSSQL are currently supported");
    }
    if (dataSourceInfo.getUseDelimiter()) {
      p.setDelimitedIdentifierModeOn(true);
    }
    log
      .info(String.format("Initialized %s platform", dataSourceInfo.getType()));
    return p;
  }
}
