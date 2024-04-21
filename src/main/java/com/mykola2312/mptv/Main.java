package com.mykola2312.mptv;

import com.mykola2312.mptv.config.Config;
import com.mykola2312.mptv.config.SourceItem;
import com.mykola2312.mptv.db.DB;
import com.mykola2312.mptv.db.M3ULoader;
import com.mykola2312.mptv.parser.M3U;
import com.mykola2312.mptv.parser.M3UException;
import com.mykola2312.mptv.parser.M3UParser;
import com.mykola2312.mptv.ui.MainFrame;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import org.flywaydb.core.Flyway;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) {
        // parse command line
        final Options options = new Options();
        options.addOption(Option
                .builder("c")
                .longOpt("config")
                .required(false)
                .hasArg()
                .build());
        CommandLine cmd;
        try {
            cmd = new DefaultParser().parse(options, args);
        } catch (ParseException e) {
            logger.fatal(e.toString());
            System.exit(1);
            return;
        }

        // load config
        final String configPath = cmd.hasOption('c') ? cmd.getOptionValue('c') : "config.json";
        Config config;
        try {
            config = Config.loadConfig(configPath);
        } catch (IOException e) {
            logger.fatal(String.format("failed to read config: %s\n", e.toString()));
            System.exit(1);
            return;
        }

        if (config.db == null) {
            logger.fatal("no database configuration. shutting down.");
            System.exit(1);
            return;
        }
        
        // setup db
        try {
            DB.setupFromConfig(config.db);
        } catch (RuntimeException e) {
            logger.fatal("setupFromConfig", e);
            logger.fatal("failed to initialize database. shutting down");
            System.exit(1);
            return;
        }

        // migrate db
        Flyway flyway = new Flyway(
                Flyway.configure()
                        .dataSource(DB.URL, DB.USER, DB.PASSWORD)
                        .baselineOnMigrate(true)
                        .load()
                        .getConfiguration()
        );
        flyway.migrate();

        // load sources, start crawlers
        for (SourceItem source : config.sources) {
            switch (source.type) {
                case M3U_LOCAL -> {
                    try {
                        if (source.path == null) {
                            logger.error("m3u local has to have \"path\" variable");
                            continue;
                        } else if (source.rootCategory == null) {
                            logger.error("source has to have \"rootCategory\"");
                            continue;
                        }

                        String m3uData = Files.readString(Paths.get(source.path), StandardCharsets.UTF_8);
                        ArrayList<M3U> m3u = M3UParser.parse(m3uData);

                        M3ULoader.loadAll(m3u, source.rootCategory);
                    } catch (IOException e) {
                        logger.error(e);
                        logger.error(String.format("failed to read local m3u file: %s", e.getMessage()));
                    } catch (M3UException e) {
                        logger.error(e);
                        logger.error(String.format("failed to parse m3u: %s", e.getMessage()));
                    }
                }

                default -> {
                    logger.error(String.format("source type %s is not implemented yet :(", source.type.name()));
                }
            }
        }

        // initialize ui
        MainFrame frame = new MainFrame();
        frame.create(config.frame);

        logger.info("mptv started");
    }
}
