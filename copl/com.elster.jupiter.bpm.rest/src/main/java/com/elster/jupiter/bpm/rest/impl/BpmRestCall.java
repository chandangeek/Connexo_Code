package com.elster.jupiter.bpm.rest.impl;

import org.ow2.util.base64.Base64;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class BpmRestCall {

    private HttpURLConnection httpConnection;
    private String jsonContent;

    public BpmRestCall() {
        this.jsonContent = "";
    }

    void doGet(String targetURL){
        try {
            String basicAuth = "Basic " + new String(Base64.encode((BpmStartup.getInstance().getUser() + ":" + BpmStartup.getInstance().getPassword()).getBytes()));
            URL targetUrl = new URL(BpmStartup.getInstance().getUrl() + targetURL);
            httpConnection = (HttpURLConnection) targetUrl.openConnection();
            httpConnection.setDoOutput(true);
            httpConnection.setRequestMethod("GET");
            httpConnection.setRequestProperty("Authorization", basicAuth);
            httpConnection.setRequestProperty ("Accept", "application/json");
            if ( httpConnection.getResponseCode() != 200 ) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + httpConnection.getResponseCode());
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (httpConnection.getInputStream())));

            String output;
            while ((output = br.readLine()) != null) {
                this.jsonContent += output;
            }

        } catch ( IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            if (httpConnection != null){
                httpConnection.disconnect();
            }
        }
    }

    public String getJsonContent() {
        return jsonContent;
    }

    public void clearJsonContent() {
        jsonContent = "";
    }


}
