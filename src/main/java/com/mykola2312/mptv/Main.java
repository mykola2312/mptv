package com.mykola2312.mptv;

import com.mykola2312.mptv.crawler.Crawler;
import com.mykola2312.mptv.db.DB;
import com.mykola2312.mptv.task.ProcessService;
import com.mykola2312.mptv.task.TaskDispatcher;
import com.mykola2312.mptv.ui.MainFrame;
import com.mykola2312.mptv.ui.MenuAction;

import org.apache.commons.cli.*;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static ProcessService processService = new ProcessService();

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
            logger.error(e.toString());
            System.exit(1);
            return;
        }

        // load config
        final String configPath = cmd.hasOption('c') ? cmd.getOptionValue('c') : "config.json";
        Config config;
        try {
            config = Config.loadConfig(configPath);
        } catch (IOException e) {
            logger.error(String.format("failed to read config: %s\n", e.toString()));
            System.exit(1);
            return;
        }

        if (config.db == null) {
            logger.error("no database configuration. shutting down.");
            System.exit(1);
            return;
        }
        
        // setup db
        try {
            DB.setupFromConfig(config.db);
        } catch (RuntimeException e) {
            logger.error("setupFromConfig", e);
            logger.error("failed to initialize database. shutting down");
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
        // Crawler crawler = new Crawler();
        // crawler.updateSources(config.sources);
        // crawler.crawl();

        // task dispatcher
        TaskDispatcher dispatcher = new TaskDispatcher();
        dispatcher.updateTaskConfig(config.tasks);
        //dispatcher.registerTask(crawler); // TODO: enable

        dispatcher.registerTask(processService);

        new Thread(dispatcher).start();

        // initialize ui
        MainFrame frame = new MainFrame();
        frame.create(config.frame);

        logger.info("mptv started");

        // TODO: remove this mock thread test
        new Thread(new Runnable() {
            @Override()
            public void run() {
                try { Thread.sleep(5000L); } catch (InterruptedException e) {}
                
                frame.action(MenuAction.ACTION_RIGHT);
            }
        }).start();

        frame.loop();
    }
}
