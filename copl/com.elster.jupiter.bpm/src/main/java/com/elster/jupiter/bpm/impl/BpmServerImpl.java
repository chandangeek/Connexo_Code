package com.elster.jupiter.bpm.impl;

import com.elster.jupiter.bpm.BpmServer;
import org.osgi.framework.BundleContext;
import org.ow2.util.base64.Base64;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class BpmServerImpl implements BpmServer {

    private static final String BPM_URL = "com.elster.jupiter.bpm.url";
    private static final String BPM_USER = "com.elster.jupiter.bpm.user";
    private static final String BPM_PASSWORD = "com.elster.jupiter.bpm.password";

    private static final String DEFAULT_BPM_URL = "http://localhost:8081/jbpm-console";
    private static final String DEFAULT_BPM_USER = "admin";
    private static final String DEFAULT_BPM_PASSWORD = "admin";

    private String url;
    private String authString;

    BpmServerImpl(BundleContext context) {
        url = context.getProperty(BPM_URL);
        if (url == null) {
            url = DEFAULT_BPM_URL;
        }
        String user = context.getProperty(BPM_USER);
        String password = context.getProperty(BPM_PASSWORD);
        if (user == null) {
            user = DEFAULT_BPM_USER;
        }
        if (password == null) {
            password = DEFAULT_BPM_PASSWORD;
        }
        this.authString = "Basic " + new String(Base64.encode((user + ":" + password).getBytes()));
    }

    public String getUrl() {
        return url;
    }

    public String doGet(String targetURL) throws IOException {
        HttpURLConnection httpConnection = null;
        try {
            URL targetUrl = new URL(url + targetURL);
            httpConnection = (HttpURLConnection) targetUrl.openConnection();
            httpConnection.setDoOutput(true);
            httpConnection.setRequestMethod("GET");
            httpConnection.setRequestProperty("Authorization", authString);
            httpConnection.setRequestProperty("Accept", "application/json");
            if (httpConnection.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + httpConnection.getResponseCode());
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (httpConnection.getInputStream())));

            String output;
            StringBuilder jsonContent = new StringBuilder();
            while ((output = br.readLine()) != null) {
                jsonContent.append(output);
            }
            return jsonContent.toString();

        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }

    public void doPost(String targetURL) {
        HttpURLConnection httpConnection = null;
        try {

            URL targetUrl = new URL(url + targetURL);
            httpConnection = (HttpURLConnection) targetUrl.openConnection();
            httpConnection.setDoOutput(true);
            httpConnection.setRequestMethod("POST");
            httpConnection.setRequestProperty("Authorization", authString);
            if (httpConnection.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + httpConnection.getResponseCode());
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getStackTrace().toString());
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }
}

