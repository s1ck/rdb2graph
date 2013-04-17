package org.graphbi.rdb2graph;

import java.io.IOException;

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
import org.graphbi.rdb2graph.transformation.Transformer;
import org.graphbi.rdb2graph.transformation.wrapper.GDBWrapperFactory;
import org.graphbi.rdb2graph.transformation.wrapper.RDBPlatformFactory;
import org.graphbi.rdb2graph.transformation.wrapper.Wrapper;
import org.graphbi.rdb2graph.util.Config;
import org.graphbi.rdb2graph.util.DataSinkInfo;
import org.graphbi.rdb2graph.util.DataSourceInfo;

@SuppressWarnings("static-access")
public class RDB2Graph {

    private static Logger log = Logger.getLogger(RDB2Graph.class);

    private static Options options;

    private static final String CONFIG_OPTION = "c";
    private static final String CONFIG_LONG_OPT = "config";
    private static final String HELP_OPTION = "h";
    private static final String HELP_LONG_OPT = "help";
    private static final String EXTRACT_OPTION = "e";
    private static final String EXTRACT_LONG_OPT = "extract";
    private static final String READ_OPTION = "r";
    private static final String READ_LONG_OPT = "read";
    private static final String TRANSFORM_OPTION = "t";
    private static final String TRANSFORM_LONG_OPT = "transform";

    static {
	options = new Options();

	Option help = new Option(HELP_OPTION, HELP_LONG_OPT, false,
		"display this information");

	Option config = OptionBuilder.withArgName("file").hasArg()
		.withDescription("use given file for config")
		.withLongOpt(CONFIG_LONG_OPT).create(CONFIG_OPTION);

	Option extract = OptionBuilder.withArgName("file").hasArg()
		.withDescription("extract ddl into given file")
		.withLongOpt(EXTRACT_LONG_OPT).create(EXTRACT_OPTION);

	Option read = OptionBuilder.withArgName("file").hasArg()
		.withDescription("read ddl from file")
		.withLongOpt(READ_LONG_OPT).create(READ_OPTION);

	Option transform = OptionBuilder
		.hasArg(false)
		.withDescription(
			"transform relational database into graph database")
		.withLongOpt(TRANSFORM_LONG_OPT).create(TRANSFORM_OPTION);

	options.addOption(help);
	options.addOption(config);
	options.addOption(extract);
	options.addOption(read);
	options.addOption(transform);
    }

    public static void writeDatabaseToXML(Database db, String fileName) {
	log.info(String.format("Writing database %s to file %s", db.getName(),
		fileName));
	new DatabaseIO().write(db, fileName);
    }

    public static Database readDatabaseFromXML(String fileName) {
	log.info(String
		.format("Reading database schema from file %s", fileName));
	return new DatabaseIO().read(RDB2Graph.class.getResource(
		"/" + fileName).getFile());
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
		.getOptionValue(CONFIG_OPTION) : "/config.xml";

	Config cfg = new Config(RDB2Graph.class.getResource(cfgFile)
		.getFile());
	cfg.parse();

	DataSourceInfo dataSourceInfo = cfg.getDataSourceInfo();
	DataSinkInfo dataSinkInfo = cfg.getDataSinkInfo();

	// set up relational database system
	Platform rdbs = RDBPlatformFactory.getInstance(dataSourceInfo);
	// set up graph database system
	Wrapper gdbs = GDBWrapperFactory.getInstance(dataSinkInfo);

	// set up relational database model (from live sys or from file)
	Database rDatabase;
	if (cmd.hasOption(READ_OPTION)) {
	    rDatabase = readDatabaseFromXML(cmd.getOptionValue(READ_OPTION));
	} else {
	    rDatabase = rdbs
		    .readModelFromDatabase(dataSourceInfo.getDatabase());
	}

	// write ddl if necessary
	if (cmd.hasOption(EXTRACT_OPTION)) {
	    writeDatabaseToXML(rDatabase, cmd.getOptionValue(EXTRACT_OPTION));
	}

	// transform is necessary
	if (cmd.hasOption(TRANSFORM_OPTION)) {
	    Transformer t = new Transformer(rdbs, rDatabase, gdbs,
		    dataSourceInfo.getUseSchema(),
		    dataSourceInfo.getUseDelimiter());

	    t.transform();
	}
    }
}
