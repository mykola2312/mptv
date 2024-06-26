package com.mykola2312.mptv.crawler;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebContent {
    public int status;
    public String body = null;

    private static final Logger logger = LoggerFactory.getLogger(WebContent.class);

    public WebContent(HttpResponse response) {
        this.status = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            try {
                this.body = EntityUtils.toString(entity, "UTF-8");
            } catch (Exception e) {
                logger.warn(String.format("failed to get content for %s: %s", response.toString()));
                logger.warn(e.toString());
            }
        }
    }
}
