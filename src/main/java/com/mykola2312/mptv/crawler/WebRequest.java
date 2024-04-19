package com.mykola2312.mptv.crawler;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

public class WebRequest {
    private final HttpGet httpGet;

    public WebRequest(String url) {
        httpGet = new HttpGet(url);
    }

    public WebContent fetch() throws WebException {
        try {
            HttpClient client = HttpClientFactory.getHttpsClient();
            HttpResponse response = client.execute(httpGet);
            
            return new WebContent(response);
        } catch (ClientProtocolException e) {
            throw new WebException(e);
        } catch (IOException e) {
            throw new WebException(e);
        }
    }
}
