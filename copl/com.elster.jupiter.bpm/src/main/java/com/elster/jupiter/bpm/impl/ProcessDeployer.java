package com.elster.jupiter.bpm.impl;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

/**
 * Main class to deploy the predefined processes during Connexo install
 */
public class ProcessDeployer {
    public static void main(String args[]) {
        if (args.length < 3) {
            System.out.println("Incorrect syntax. The following parameters are required:");
            System.out.println("url -- url to the Connexo Flow installation");
            System.out.println("user -- a Conenxo Flow user with administrative privileges");
            System.out.println("password -- password for the provided user");
            return;
        }

        String authString = "Basic " + new String(Base64.getEncoder().encode((args[1] + ":" + args[2]).getBytes()));

        deployProcess("org.jbpm:notifyuser:1.0:defaultKieBase:defaultKieSession", args[0], authString);
        deployProcess("org.jbpm:sendsomeone:1.0:defaultKieBase:defaultKieSession", args[0], authString);
    }

    private static void deployProcess(String deploymentId, String arg, String authString) {
        String url = arg + "/rest/deployment/" + deploymentId + "/deploy?strategy=SINGLETON";
        doPost(url, authString);
    }

    private static void doPost(String url, String authString) {
        HttpURLConnection httpConnection = null;
        try {

            URL targetUrl = new URL(url);
            httpConnection = (HttpURLConnection) targetUrl.openConnection();
            httpConnection.setDoOutput(true);
            httpConnection.setRequestMethod("POST");
            httpConnection.setRequestProperty("Authorization", authString);
            httpConnection.setRequestProperty("Content-Type", "text/plain;charset=UTF-8");

            if (httpConnection.getResponseCode() != 202) {
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
