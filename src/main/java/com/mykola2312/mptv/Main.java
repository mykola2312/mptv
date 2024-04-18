package com.mykola2312.mptv;

import com.mykola2312.mptv.config.Config;
import com.mykola2312.mptv.ui.MainFrame;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import org.flywaydb.core.Flyway;
import static com.mykola2312.mptv.tables.Test.*;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;

public class Main {
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
            System.err.println(e.toString());
            System.exit(1);
            return;
        }

        final String configPath = cmd.hasOption('c') ? cmd.getOptionValue('c') : "config.json";
        Config config;
        try {
            config = Config.loadConfig(configPath);
        } catch (IOException e) {
            System.err.printf("failed to read config: %s\n", e.toString());
            System.exit(1);
            return;
        }

        MainFrame frame = new MainFrame();
        frame.create(config.frame);

        Flyway flyway = new Flyway(
                Flyway.configure()
                        .dataSource("jdbc:sqlite:mptv.db", "", "")
                        .load()
                        .getConfiguration()
        );
        flyway.migrate();

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:mptv.db", "", "")) {
            DSLContext create = DSL.using(conn, SQLDialect.SQLITE);
            Result<Record> result = create.select().from(TEST).fetch();
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Logger logger = Logger.getLogger(Main.class);
        logger.info("mptv started");
    }
}
