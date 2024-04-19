package com.mykola2312.mptv.crawler;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;

// TODO: make more useful this stub exception
public class WebException extends RuntimeException {
    public enum Type {
        FAILED_TO_MAKE_CLIENT,
        FETCH_FAILURE,
        IO_ERROR,
    };

    public final Type type;

    protected static final Logger logger = Logger.getLogger(WebException.class);
    protected void log() {
        logger.warn(String.format("%s: %s", type.name(), this.getCause().getMessage()));
    }

    public WebException(NoSuchAlgorithmException e) {
        super(Type.FAILED_TO_MAKE_CLIENT.name(), e);
        this.type = Type.FAILED_TO_MAKE_CLIENT;
    }

    public WebException(KeyManagementException e) {
        super(Type.FAILED_TO_MAKE_CLIENT.name(), e);
        this.type = Type.FAILED_TO_MAKE_CLIENT;
    }

    public WebException(ClientProtocolException e) {
        super(Type.FETCH_FAILURE.name(), e);
        this.type = Type.FETCH_FAILURE;
    }

    public WebException(IOException e) {
        super(Type.IO_ERROR.name(), e);
        this.type = Type.IO_ERROR;
    }
}
