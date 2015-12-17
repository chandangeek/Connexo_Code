package com.elster.partners.connexo.filters.generic;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Created by dragos on 11/26/2015.
 */
public class ConnexoRestProxyManager {
    private final String url;
    private String authorization;

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

    public String getConnexoAuthorizationToken(){
        return doPost("/api/apps/apps/login");
    }

    public void setAuthorization(String authorization){
        this.authorization = authorization;
    }

    private ConnexoRestProxyManager(String url) {
        this.url = url;
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
                return null;
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
