package org.graphbi.rdb2graph;

import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.io.DatabaseIO;
import org.apache.ddlutils.model.Database;
import org.apache.log4j.Logger;
import org.graphbi.rdb2graph.analysis.documentgraph.DocGraph;
import org.graphbi.rdb2graph.analysis.documentgraph.DocGraphExtractor;
import org.graphbi.rdb2graph.analysis.documentgraph.analyzer.AnalyzerResult;
import org.graphbi.rdb2graph.analysis.documentgraph.analyzer.DocGraphMeasureFunction;
import org.graphbi.rdb2graph.analysis.documentgraph.analyzer.NumericalAggregation;
import org.graphbi.rdb2graph.analysis.documentgraph.analyzer.StatisticsLogger;
import org.graphbi.rdb2graph.transformation.Transformer;
import org.graphbi.rdb2graph.util.config.Config;
import org.graphbi.rdb2graph.util.config.DataSinkInfo;
import org.graphbi.rdb2graph.util.config.DataSourceInfo;
import org.graphbi.rdb2graph.util.graph.ReadOnlyGraph;
import org.graphbi.rdb2graph.util.graph.ReadOnlyGraphFactory;
import org.graphbi.rdb2graph.util.graph.ReadWriteGraph;
import org.graphbi.rdb2graph.util.graph.ReadWriteGraphFactory;
import org.graphbi.rdb2graph.util.rdb.RelationalDatabasePlatformFactory;

@SuppressWarnings("static-access")
public class RDB2Graph {

    private static Logger log = Logger.getLogger(RDB2Graph.class);

    private static Options options;

    private static final String CONFIG_OPTION = "c";
    private static final String CONFIG_LONG_OPT = "config";
    private static final String HELP_OPTION = "h";
    private static final String HELP_LONG_OPT = "help";
    private static final String EXPORT_DDL_OPTION = "e";
    private static final String EXPORT_LONG_OPT = "export-ddl";
    private static final String READ_OPTION = "r";
    private static final String READ_LONG_OPT = "read-ddl";
    private static final String TRANSFORM_OPTION = "t";
    private static final String TRANSFORM_LONG_OPT = "transform";
    private static final String ANALYZE_OPTION = "a";
    private static final String ANALYZE_LONG_OPTION = "analyze";

    static {
	options = new Options();

	Option help = new Option(HELP_OPTION, HELP_LONG_OPT, false,
		"Display this information.");
	Option config = OptionBuilder.withArgName("file").hasArg()
		.withDescription("Use the given file as config file.")
		.withLongOpt(CONFIG_LONG_OPT).create(CONFIG_OPTION);
	Option export = OptionBuilder.withArgName("file").hasArg()
		.withDescription("Export DDL into given file.")
		.withLongOpt(EXPORT_LONG_OPT).create(EXPORT_DDL_OPTION);
	Option read = OptionBuilder.withArgName("file").hasArg()
		.withDescription("Read DDL from file.")
		.withLongOpt(READ_LONG_OPT).create(READ_OPTION);
	Option transform = OptionBuilder
		.hasArg(false)
		.withDescription(
			"Transform relational database into graph database.")
		.withLongOpt(TRANSFORM_LONG_OPT).create(TRANSFORM_OPTION);
	Option analyze = OptionBuilder.hasArg()
		.withDescription("Analzye the graph. Possible args: [opgraph]")
		.withLongOpt(ANALYZE_LONG_OPTION).create(ANALYZE_OPTION);

	options.addOption(help);
	options.addOption(config);
	options.addOption(export);
	options.addOption(read);
	options.addOption(transform);
	options.addOption(analyze);
    }

    public static void writeDatabaseToXML(Database db, String fileName) {
	log.info(String.format("Writing database %s to file %s", db.getName(),
		fileName));
	new DatabaseIO().write(db, fileName);
    }

    public static Database readDatabaseFromXML(String fileName) {
	log.info(String
		.format("Reading database schema from file %s", fileName));
	return new DatabaseIO().read(RDB2Graph.class
		.getResource("/" + fileName).getFile());
    }

    public static void main(String[] args) throws IOException, ParseException {
	CommandLineParser parser = new BasicParser();
	CommandLine cmd = parser.parse(options, args);
	// help
	if (cmd.hasOption(HELP_OPTION) || args.length == 0) {
	    HelpFormatter formatter = new HelpFormatter();
	    formatter.printHelp("rdb2graph", options);
	    System.exit(0);
	}

	String cfgFile = cmd.hasOption(CONFIG_OPTION) ? cmd
		.getOptionValue(CONFIG_OPTION) : "/config.properties";

	Config cfg = new Config(RDB2Graph.class.getResource(cfgFile).getFile());
	cfg.parse();

	DataSourceInfo dataSourceInfo = cfg.getRelationalStore();
	DataSinkInfo dataSinkInfo = cfg.getGraphStore();

	// set up relational database system
	Platform rdbs = RelationalDatabasePlatformFactory
		.getInstance(dataSourceInfo);

	// set up relational database model (from live sys or from file)
	Database rDatabaseSchema;
	if (cmd.hasOption(READ_OPTION)) {
	    rDatabaseSchema = readDatabaseFromXML(cmd
		    .getOptionValue(READ_OPTION));
	} else {
	    rDatabaseSchema = rdbs.readModelFromDatabase(dataSourceInfo
		    .getDatabase());
	}
	// write ddl if necessary
	if (cmd.hasOption(EXPORT_DDL_OPTION)) {
	    writeDatabaseToXML(rDatabaseSchema,
		    cmd.getOptionValue(EXPORT_DDL_OPTION));
	}
	// transform is necessary
	if (cmd.hasOption(TRANSFORM_OPTION)) {
	    // set up graph database system
	    ReadWriteGraph gdbs = ReadWriteGraphFactory
		    .getInstance(dataSinkInfo);
	    // and transform relational database into graph
	    Transformer t = new Transformer(cfg, rdbs, rDatabaseSchema, gdbs,
		    dataSourceInfo.getUseSchema(),
		    dataSourceInfo.getUseDelimiter());
	    t.transform();
	}
	// analyze if necessary
	if (cmd.hasOption(ANALYZE_OPTION)) {
	    // set up graph database system
	    ReadOnlyGraph gdbs = ReadOnlyGraphFactory.getInstance(dataSinkInfo);
	    String arg = cmd.getOptionValue(ANALYZE_OPTION).toLowerCase();
	    if ("opgraph".equals(arg)) {
		DocGraphExtractor opGraphExtractor = new DocGraphExtractor(
			gdbs, rDatabaseSchema);
		// extract and analyze the results
		List<DocGraph> docGraphs = opGraphExtractor.extract();
		new StatisticsLogger().analyze(docGraphs);

		List<AnalyzerResult<Integer>> res = new NumericalAggregation()
			.analyze(docGraphs,
				new DocGraphMeasureFunction<Integer>() {

				    @Override
				    public Integer measure(DocGraph docGraph) {
					return docGraph.getNodeCount()
						+ docGraph.getEdgeCount();
				    }
				});
		
		for (AnalyzerResult<Integer> r : res) {
		    log.info(r.getDocGraph().getId() + " -> " + r.getResult());
		}

		// copy them into the dedicated graph store
		ReadWriteGraph targetGraphDB = ReadWriteGraphFactory
			.getInstance(cfg.getOpGraphStore());
		// new DocGraphDuplicator(cfg, gdbs, targetGraphDB)
		// .duplicate(docGraphs);
	    }
	}
    }
}
