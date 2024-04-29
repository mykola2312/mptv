package com.mykola2312.mptv.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.checkerframework.checker.nullness.qual.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import org.jooq.ConnectionProvider;
import org.jooq.exception.DataAccessException;

// I can't believe that in 2024 I have to do it myself
public class DBPool implements ConnectionProvider {
    private String url;

    private static final Logger logger = LoggerFactory.getLogger(DBPool.class);

    private class DBConnection {
        public final Connection connection;
        public boolean beingUsed;

        public DBConnection(Connection connection, boolean beingUsed) {
            this.connection = connection;
            this.beingUsed = beingUsed;
        }
    }

    public static final int POOL_LOW_CAP = 4;
    private ArrayList<DBConnection> connections = new ArrayList<DBConnection>(POOL_LOW_CAP);

    public DBPool(String url) throws RuntimeException {
        this.url = url;

        // allocate pool now
        try {
            for (int i = 0; i < POOL_LOW_CAP; i++) {
                spawnConnection();
            }
        } catch (SQLException e) {
            throw new RuntimeException("failed to allocate pool", e);
        }
    }

    protected DBConnection spawnConnection() throws SQLException {
        DBConnection conn = new DBConnection(DriverManager.getConnection(url), false);
        connections.add(conn);
        return conn;
    }

    @Override
    public @Nullable Connection acquire() throws DataAccessException {
        logger.debug("acquire");
        DBConnection conn;
        try {
            conn = connections
                .stream()
                .filter(db -> !db.beingUsed)
                .findFirst()
                .get();
        } catch (NoSuchElementException e) {
            try {
                conn = spawnConnection();
            } catch (SQLException e1) {
                throw new DataAccessException("failed to spawn connection", e1);
            }
        }

        conn.beingUsed = true;

        return conn.connection;
    }

    @Override
    public void release(Connection conn) throws DataAccessException {
        try {
            DBConnection db = connections
                .stream()
                .filter(dbConn -> conn == dbConn.connection)
                .findFirst()
                .get();
            
            db.beingUsed = false;
            if (connections.size() > POOL_LOW_CAP) {
                connections.remove(db);
            }

            logger.debug(String.format("release %s", db.toString()));
        } catch (NoSuchElementException e) {
            throw new DataAccessException("connection is not present in pool");
        }
    }
    
}
