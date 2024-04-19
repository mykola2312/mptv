package com.mykola2312.mptv.db;

import org.checkerframework.checker.nullness.qual.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;

import org.jooq.ConnectionProvider;
import org.jooq.exception.DataAccessException;

// I can't believe that in 2024 I have to do it myself
public class DBPool implements ConnectionProvider {
    private String url;
    private String user;
    private String password;

    public static final int POOL_LIMIT = 4;
    private LinkedList<Connection> connections;

    public DBPool(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
        
        connections = new LinkedList<Connection>();
    }

    protected Connection spawnConnection() throws SQLException {
        return null;
    }

    @Override
    public @Nullable Connection acquire() throws DataAccessException {
        throw new UnsupportedOperationException("Unimplemented method 'acquire'");
    }

    @Override
    public void release(Connection arg0) throws DataAccessException {
        throw new UnsupportedOperationException("Unimplemented method 'release'");
    }
    
}
