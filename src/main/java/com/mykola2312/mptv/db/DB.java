package com.mykola2312.mptv.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.mykola2312.mptv.config.DBConfig;

public class DB {
    public static String URL = "jdbc:sqlite:mptv.db";
    public static String USER = "";
    public static String PASSWORD = "";

    public static void setupFromConfig(DBConfig config) {
        URL = config.url;
        USER = config.user;
        PASSWORD = config.password;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
