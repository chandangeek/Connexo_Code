/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.partners.connexo.filters.generic;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by dragos on 11/26/2015.
 */
public class ConnexoRestProxyManager {
    private final String url;
    private static ConnexoRestProxyManager instance = null;

    public static synchronized ConnexoRestProxyManager getInstance(String url) {
        if(instance == null) {
            instance = new ConnexoRestProxyManager(url);
        }

        return instance;
    }

    public static synchronized ConnexoRestProxyManager getInstance() {
        return instance;
    }

    public String getConnexoAuthorizationToken(String authorization){
        return doPost("/api/apps/apps/login", authorization);
    }

    private ConnexoRestProxyManager(String url) {
        this.url = url;
    }

    private String doPost(String targetURL, String authorization) {
        HttpURLConnection httpConnection = null;
        try {
            URL connexoUrl = new URL(url + targetURL);
            httpConnection = (HttpURLConnection) connexoUrl.openConnection();
            httpConnection.setRequestMethod("POST");
            httpConnection.setRequestProperty("Authorization", authorization);
            httpConnection.setRequestProperty("Accept", "application/json");
            if (httpConnection.getResponseCode() != 204) {
                return null;
            }

            return httpConnection.getHeaderField("X-AUTH-TOKEN");

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }
}
