package com.mykola2312.mptv.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.jooq.Configuration;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;

public class DB {
    public static String URL = "jdbc:sqlite:mptv.db";
    public static String USER = "";
    public static String PASSWORD = "";

    public static DBPool POOL;
    public static Configuration CONFIG;

    public static void setupFromConfig(DBConfig config) throws RuntimeException {
        URL = config.url;
        USER = config.user;
        PASSWORD = config.password;

        POOL = new DBPool(URL);
        CONFIG = new DefaultConfiguration()
            .set(POOL)
            .set(SQLDialect.SQLITE);
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
