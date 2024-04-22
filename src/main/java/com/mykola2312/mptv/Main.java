package com.mykola2312.mptv;

import com.mykola2312.mptv.config.Config;
import com.mykola2312.mptv.crawler.Crawler;
import com.mykola2312.mptv.db.DB;
import com.mykola2312.mptv.ui.MainFrame;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import org.flywaydb.core.Flyway;

import java.io.IOException;

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
        Crawler crawler = new Crawler(config.sources);
        crawler.crawl();

        // initialize ui
        MainFrame frame = new MainFrame();
        frame.create(config.frame);

        logger.info("mptv started");
    }
}
