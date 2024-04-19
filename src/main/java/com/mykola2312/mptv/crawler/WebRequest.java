package com.mykola2312.mptv.crawler;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

public class WebRequest {
    private final HttpGet httpGet;

    public WebRequest(String url) {
        httpGet = new HttpGet(url);
    }

    public String fetch() {
        try {
            HttpClient client = HttpClientFactory.getHttpsClient();
            HttpResponse response = client.execute(httpGet);
            
            HttpEntity entity = response.getEntity();
            return EntityUtils.toString(entity, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
