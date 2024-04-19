package com.mykola2312.mptv.crawler;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;

public class HttpClientFactory {
    private static CloseableHttpClient client = null;
    private static final String[] SUPPORTED_PROTOCOLS = {
        "TLSv1.3",
        "TLSv1.2",
        "TLSv1.1",
        "TLSv1",
        "SSLv3",
        "DTLSv1.2",
        "DTLSv1.0",
    };

    public static HttpClient getHttpsClient() throws WebException {
        if (client != null) {
            return client;
        }

        try {
            SSLContext sslContext = SSLContexts
                .custom()
                .build();
            sslContext.init(null, new TrustManager[] { new HttpsTrustManager() }, new SecureRandom());

            SSLConnectionSocketFactory factory = new SSLConnectionSocketFactory(sslContext,
                SUPPORTED_PROTOCOLS,
                null,
                SSLConnectionSocketFactory.getDefaultHostnameVerifier()
            );
            client = HttpClients
                .custom()
                .setSSLSocketFactory(factory)
                .build();
        } catch (NoSuchAlgorithmException e) {
            throw new WebException(e);
        } catch (KeyManagementException e) {
            throw new WebException(e);
        }

        return client;
    }
}
