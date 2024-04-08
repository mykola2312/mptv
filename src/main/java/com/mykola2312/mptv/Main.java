package com.mykola2312.mptv;

import com.mykola2312.mptv.ui.MainFrame;
import org.apache.commons.cli.*;

import java.io.IOException;

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
        frame.create(config.frame.width, config.frame.height, config.frame.fullscreen);
    }
}
