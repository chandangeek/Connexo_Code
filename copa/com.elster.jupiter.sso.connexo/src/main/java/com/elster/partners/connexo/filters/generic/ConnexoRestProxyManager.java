package com.elster.partners.connexo.filters.generic;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by dragos on 11/26/2015.
 */
public class ConnexoRestProxyManager {
    private final String url;
    private final String authorization;

    private static ConnexoRestProxyManager instance = null;

    public static synchronized ConnexoRestProxyManager getInstance(String url, String authorization) {
        if(instance == null) {
            instance = new ConnexoRestProxyManager(url, authorization);
        }

        return instance;
    }

    public String getToken(){
        return doPost("/api/apps/apps/login");
    }

    private ConnexoRestProxyManager(String url, String authorization) {
        this.url = url;
        this.authorization = authorization;
    }

    private String doPost(String targetURL) {
        HttpURLConnection httpConnection = null;
        try {
            URL connexoUrl = new URL(url + targetURL);
            httpConnection = (HttpURLConnection) connexoUrl.openConnection();
            httpConnection.setRequestMethod("POST");
            httpConnection.setRequestProperty("Authorization", this.authorization);
            httpConnection.setRequestProperty("Accept", "application/json");
            if (httpConnection.getResponseCode() != 204) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + httpConnection.getResponseCode());
            }

            return httpConnection.getHeaderField("X-AUTH-TOKEN");

        } catch (Exception e) {
            throw new RuntimeException(e.getStackTrace().toString());
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }
}
