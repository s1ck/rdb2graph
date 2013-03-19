package org.graphbi.rdb2graph;

import java.io.IOException;

import org.graphbi.rdb2graph.transformer.Transformer;
import org.graphbi.rdb2graph.util.Config;
import org.graphbi.rdb2graph.util.DataSinkInfo;
import org.graphbi.rdb2graph.util.DataSourceInfo;

public class TransformerApp {

    public static void main(String[] args) throws IOException {
	Config cfg = new Config(TransformerApp.class.getResource("/config.xml")
		.getFile());
	cfg.parse();

	DataSourceInfo dataSourceInfo = cfg.getDataSourceInfo();
	DataSinkInfo dataSinkInfo = cfg.getDataSinkInfo();

	Transformer t = new Transformer(dataSourceInfo, dataSinkInfo,
		cfg.getLinkTableInfos());

	t.transform();
    }
}
