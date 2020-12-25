/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.impl;

import com.elster.jupiter.bpm.HttpException;
import com.elster.jupiter.bpm.BpmServer;
import com.elster.jupiter.bpm.ProcessInstanceInfos;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BpmServerImpl implements BpmServer {

    private static final String BPM_URL = "com.elster.jupiter.bpm.url";
    private static final String BPM_EXTERNAL_URL = "com.elster.jupiter.bpm.externalurl";
    private static final String BPM_USER = "com.elster.jupiter.bpm.user";
    private static final String BPM_PASSWORD = "com.elster.jupiter.bpm.password";

    private static final String DEFAULT_BPM_URL = "http://localhost:8081/flow";
    private static final String DEFAULT_BPM_USER = "admin";
    private static final String DEFAULT_BPM_PASSWORD = "admin";

    private String externalUrl;
    private String url;
    private String basicAuthString;
    private String staticTokenAuth;

    private volatile ThreadPrincipalService threadPrincipalService;

    private Logger logger = Logger.getLogger(this.getClass().getName());

    BpmServerImpl(BundleContext context, ThreadPrincipalService threadPrincipalService, String staticTokenAuth) {
        this.threadPrincipalService = threadPrincipalService;
        this.setUrlFromContext(context);
        this.setExternalUrlFromContext(context);
        this.staticTokenAuth = staticTokenAuth;

        String user = this.getUserFromContext(context);
        String password = this.getPasswordFromContext(context);
        if (user != null && password != null) {
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

    private void setExternalUrlFromContext(BundleContext context) {
        if (context != null) {
            externalUrl = context.getProperty(BPM_EXTERNAL_URL);
        }
        if (externalUrl == null) {
            externalUrl = url;
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
    public String getExternalUrl() {
        return externalUrl;
    }

    @Override
    public long doPost(String targetURL, String payload) {
        doPost(targetURL, payload, basicAuthString);
        return 0;
    }

    @Override
    public long doPost(String targetURL, String payload, String authorization) {
        doPost(targetURL, payload, authorization, 0);
        return 0;
    }

    @Override
    public String doPost(String targetURL, String payload, long version) {
        return doPost(targetURL, payload, basicAuthString, version);
    }

    @Override
    public String doPost(String targetURL, String payload, String authorization, long version) {
        HttpURLConnection httpConnection = null;
        authorization = (basicAuthString != null) ? basicAuthString : authorization != null ? authorization : staticTokenAuth;

        try {
            URL targetUrl = buildTargetUrl(targetURL);
            httpConnection = (HttpURLConnection) targetUrl.openConnection();
            httpConnection.setConnectTimeout(60000);
            httpConnection.setDoOutput(true);
            httpConnection.setRequestMethod("POST");
            httpConnection.setRequestProperty("Authorization", authorization);
            httpConnection.setRequestProperty("Accept", "application/json");
            httpConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            if (payload != null) {
                OutputStreamWriter osw = new OutputStreamWriter(httpConnection.getOutputStream());
                osw.write(payload);
                osw.flush();
                osw.close();
            }

            int responseCode = httpConnection.getResponseCode();
            if (responseCode < 200 || responseCode >= 300) {
                BufferedReader br = new BufferedReader(new InputStreamReader((httpConnection.getErrorStream())));
                StringBuilder jsonContent = new StringBuilder();
                String output;
                while ((output = br.readLine()) != null) {
                    jsonContent.append(output);
                }
                throw new HttpException(jsonContent.toString(), responseCode);
            } else {
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        (httpConnection.getInputStream())));
                String output;
                StringBuilder jsonContent = new StringBuilder();
                while ((output = br.readLine()) != null) {
                    jsonContent.append(output);
                }

                return jsonContent.toString();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }

    /**
     * Normalizes a final URL in order to avoid configuration mistakes around ending slash
     *  Ex:     http://localhost/flow  /rest/call
     *          http://localhost/flow/  /rest/call
     *          http://localhost/flow  rest/call
     *          http://localhost/flow/  rest/call
     * will all result in:
     *          http://localhost/flow/rest/call
     *
     * @param relativePath  path to append to main BPM url
     * @return              normalized url: bpmURL + / + relativePath while removing/adding slashes as necessary
     */
    public URL buildTargetUrl(String relativePath) throws MalformedURLException {
        String basePath = getUrl();
        String finalPath;

        if (basePath.charAt(basePath.length() - 1) == '/') {
            basePath = basePath.substring(0, basePath.length() - 1);
        }

        if (relativePath.startsWith("/")) {
            finalPath = basePath + relativePath;
        } else {
            finalPath = basePath + '/' + relativePath;
        }

        return new URL(finalPath);
    }

    @Override
    public String doGet(String targetURL) {
        return doGet(targetURL, basicAuthString);
    }

    @Override
    public String doGet(String targetURL, String authorization) {
        HttpURLConnection httpConnection = null;
        String authorizationHeader = (basicAuthString != null) ? basicAuthString : authorization != null ? authorization : staticTokenAuth;
        try {
            URL targetUrl = new URL(url + targetURL);
            getLogger().info("BpmServer: GET: " + targetUrl.toString());
            httpConnection = (HttpURLConnection) targetUrl.openConnection();
            httpConnection.setConnectTimeout(60000);
            httpConnection.setDoOutput(true);
            httpConnection.setRequestMethod("GET");
            httpConnection.setRequestProperty("Authorization", authorizationHeader);
            httpConnection.setRequestProperty("Accept", "application/json");

            int responseCode = httpConnection.getResponseCode();
            getLogger().info("BpmServer: GET: " + targetUrl.toString() + " - Response: " + responseCode);
            if (responseCode != 200 && responseCode != 204) {
                throw new RuntimeException(Integer.toString(responseCode));
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
            getLogger().log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }

    private Logger getLogger() {
        return logger;
    }

    @Override
    public ProcessInstanceInfos getRunningProcesses(String authorization, String filter) {
        String jsonContent;
        JSONArray processes = null;
        try {
            String rest = "/rest/tasks/runningprocesses";
            if (!filter.isEmpty()) {
                rest += filter.startsWith("?") ? filter : "?" + filter;
            }
            jsonContent = this.doGet(rest, authorization);
            if (!"".equals(jsonContent)) {
                JSONObject obj = new JSONObject(jsonContent);
                processes = obj.getJSONArray("processInstances");
            }

        } catch (JSONException | RuntimeException e) {
            getLogger().log(Level.SEVERE, e.getMessage(), e);
        }

        return new ProcessInstanceInfos(processes, (threadPrincipalService.getPrincipal() != null) ? threadPrincipalService
                .getPrincipal()
                .getName() : "");
    }

    private String mapProcessStatus(int status) {
        String currentStatus = "UNKNOWN";
        switch (status) {
            case 0:
                break;
        }
        return currentStatus;
    }

}

