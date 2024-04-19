package com.mykola2312.mptv;

import com.mykola2312.mptv.config.Config;
import com.mykola2312.mptv.crawler.WebRequest;
import com.mykola2312.mptv.db.DB;
import com.mykola2312.mptv.ui.MainFrame;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import org.flywaydb.core.Flyway;
import org.checkerframework.checker.nullness.qual.*;

import static com.mykola2312.mptv.tables.Test.*;
import org.jooq.*;
import org.jooq.impl.*;

import java.io.IOException;
import java.util.List;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) {
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
        
        try {
            DB.setupFromConfig(config.db);
        } catch (RuntimeException e) {
            logger.fatal("setupFromConfig", e);
            logger.fatal("failed to initialize database. shutting down");
            System.exit(1);
            return;
        }

        MainFrame frame = new MainFrame();
        frame.create(config.frame);

        Flyway flyway = new Flyway(
                Flyway.configure()
                        .dataSource(DB.URL, DB.USER, DB.PASSWORD)
                        .load()
                        .getConfiguration()
        );
        flyway.migrate();

        DSLContext create = DSL.using(DB.CONFIG);
        @NonNull List<Test> result = create
            .select()
            .from(TEST)
            .fetchInto(Test.class);
        for (Test t : result) {
            System.out.printf("%d: %s\n", t.id, t.value);
        }

        create = DSL.using(DB.CONFIG);
        Test test = create
            .select()
            .from(TEST)
            .limit(1)
            .fetchOne()
            .into(Test.class);
        System.out.printf("fetchOne -> %d: %s\n", test.id, test.value);

        WebRequest get = new WebRequest("https://example.com");
        System.out.println(get.fetch());

        logger.info("mptv started");
    }
}
