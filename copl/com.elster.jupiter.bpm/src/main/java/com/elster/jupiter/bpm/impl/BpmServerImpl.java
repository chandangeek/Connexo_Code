package com.elster.jupiter.bpm.impl;

import com.elster.jupiter.bpm.BpmServer;
import org.osgi.framework.BundleContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public class BpmServerImpl implements BpmServer {

    private static final String BPM_URL = "com.elster.jupiter.bpm.url";
    private static final String BPM_USER = "com.elster.jupiter.bpm.user";
    private static final String BPM_PASSWORD = "com.elster.jupiter.bpm.password";

    private static final String DEFAULT_BPM_URL = "http://localhost:8081/flow";
    private static final String DEFAULT_BPM_USER = "admin";
    private static final String DEFAULT_BPM_PASSWORD = "admin";

    private String url;
    private String basicAuthString;

    BpmServerImpl(BundleContext context) {
        this.setUrlFromContext(context);
        String user = this.getUserFromContext(context);
        String password = this.getPasswordFromContext(context);
        if(user != null && password != null) {
            this.basicAuthString = "Basic " + new String(Base64.getEncoder().encode((user + ":" + password).getBytes()));
        }
    }

    private void setUrlFromContext(BundleContext context) {
        if (context != null) {
            url = context.getProperty(BPM_URL);
        }
        if (url == null) {
            url = DEFAULT_BPM_URL;
        }
    }

    private String getUserFromContext(BundleContext context) {
        String user = null;
        if (context != null) {
            user = context.getProperty(BPM_USER);
        }
        return user;
    }

    private String getPasswordFromContext(BundleContext context) {
        String password = null;
        if (context != null) {
            password = context.getProperty(BPM_PASSWORD);
        }
        return password;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String doGet(String targetURL) {
        return doGet(targetURL, basicAuthString);
    }

    @Override
    public String doGet(String targetURL, String authorization) {
        HttpURLConnection httpConnection = null;
        authorization = (basicAuthString != null)?basicAuthString:authorization;
        try {
            URL targetUrl = new URL(url + targetURL);
            httpConnection = (HttpURLConnection) targetUrl.openConnection();
            httpConnection.setDoOutput(true);
            httpConnection.setRequestMethod("GET");
            httpConnection.setRequestProperty("Authorization", authorization);
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

        } catch (IOException e) {
            return e.getMessage();
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }

    @Override
    public long doPost(String targetURL, String payload) {
        return doPost(targetURL, payload, basicAuthString);
    }

    @Override
    public long doPost(String targetURL, String payload, String authorization) {
        long check = 0;
        HttpURLConnection httpConnection = null;
        authorization = (basicAuthString != null)?basicAuthString:authorization;
        try {
            URL targetUrl = new URL(url + targetURL);
            httpConnection = (HttpURLConnection) targetUrl.openConnection();
            httpConnection.setDoOutput(true);
            httpConnection.setRequestMethod("POST");
            httpConnection.setRequestProperty("Authorization", authorization);
            httpConnection.setRequestProperty("Accept", "application/json");
            httpConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            if(payload != null) {
                OutputStreamWriter osw = new OutputStreamWriter(httpConnection.getOutputStream());
                osw.write(payload);
                osw.flush();
                osw.close();
            }
            if (httpConnection.getResponseCode() != 200) {
                check = -1;
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getStackTrace().toString());
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
            return check;
        }
    }

}

