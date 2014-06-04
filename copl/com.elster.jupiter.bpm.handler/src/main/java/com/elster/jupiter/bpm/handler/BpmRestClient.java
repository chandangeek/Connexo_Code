package com.elster.jupiter.bpm.handler;

import org.ow2.util.base64.Base64;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class BpmRestClient {

    private HttpURLConnection httpConnection;

    void doGet(String targetURL) {
        //TODO: Will be implemented for the 'get' methods (i.e. getProcesses(), getProcessParameters(String processId))
    }

    void doPost(String targetURL){
        try {
            String basicAuth = "Basic " + new String(Base64.encode((BpmStatrup.getInstance().getUser()+":"+BpmStatrup.getInstance().getPassword()).getBytes()));
            URL targetUrl = new URL(BpmStatrup.getInstance().getUrl() + targetURL);
            httpConnection = (HttpURLConnection) targetUrl.openConnection();
            httpConnection.setDoOutput(true);
            httpConnection.setRequestMethod("POST");
            httpConnection.setRequestProperty ("Authorization", basicAuth);
            if ( httpConnection.getResponseCode() != 200 ) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + httpConnection.getResponseCode());
            }
        } catch ( IOException e) {
            throw new RuntimeException( e.getStackTrace().toString() );
        }
        finally {
            if (httpConnection != null){
                httpConnection.disconnect();
            }
        }
    }
}
