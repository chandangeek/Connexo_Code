package com.elster.partners.connexo.filters.generic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

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
        return doGet("/api/usr/currentuser/");
    }

    private ConnexoRestProxyManager(String url, String authorization) {
        this.url = url;
        this.authorization = authorization;
    }

    private String doGet(String targetURL) {
        HttpURLConnection httpConnection = null;
        try {
            URL targetUrl = new URL(url + targetURL);
            httpConnection = (HttpURLConnection) targetUrl.openConnection();
            httpConnection.setDoOutput(true);
            httpConnection.setRequestMethod("GET");
            httpConnection.setRequestProperty("Authorization", this.authorization);
            httpConnection.setRequestProperty("Accept", "application/json");
            if (httpConnection.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + httpConnection.getResponseCode());
            }

            return httpConnection.getHeaderField("X-CONNEXO-TOKEN");

        } catch (IOException e) {
            throw new RuntimeException(e.getStackTrace().toString());
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }
}
