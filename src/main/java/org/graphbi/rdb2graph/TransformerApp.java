package org.graphbi.rdb2graph;

import java.io.IOException;

import org.apache.ddlutils.Platform;
import org.apache.ddlutils.io.DatabaseIO;
import org.apache.ddlutils.model.Database;
import org.graphbi.rdb2graph.transformer.Transformer;
import org.graphbi.rdb2graph.util.Config;
import org.graphbi.rdb2graph.util.DataSinkInfo;
import org.graphbi.rdb2graph.util.DataSourceInfo;
import org.graphbi.rdb2graph.util.GDBWrapperFactory;
import org.graphbi.rdb2graph.util.RDBPlatformFactory;
import org.graphbi.rdb2graph.wrapper.Wrapper;

public class TransformerApp {
    
    public static void writeDatabaseToXML(Database db, String fileName)
    {
        new DatabaseIO().write(db, fileName);
    }

    public static void main(String[] args) throws IOException {
	Config cfg = new Config(TransformerApp.class.getResource("/config.xml")
		.getFile());
	cfg.parse();
	
	DataSourceInfo dataSourceInfo = cfg.getDataSourceInfo();
	DataSinkInfo dataSinkInfo = cfg.getDataSinkInfo();

	Platform platform = RDBPlatformFactory.getInstance(dataSourceInfo);
	Wrapper gDatabase = GDBWrapperFactory.getInstance(dataSinkInfo);
	Database rDatabase = platform.readModelFromDatabase(dataSourceInfo
		.getDatabase());
	
	Transformer t = new Transformer(platform, rDatabase, gDatabase,
		cfg.getLinkTableInfos(), dataSourceInfo.getUseSchema(),
		dataSourceInfo.getUseDelimiter());

	t.transform();
    }
}
