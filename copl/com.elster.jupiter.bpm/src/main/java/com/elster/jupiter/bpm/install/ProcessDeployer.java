package com.elster.jupiter.bpm.install;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

/**
 * Main class to deploy the predefined processes during Connexo install
 */
public class ProcessDeployer {

    private static final String defaultRepoPayload = "{\"name\":\"Connexo\",\"description\":\"Repository for Connexo projects\",\"userName\":null,\"password\":null,\"requestType\":\"new\",\"gitURL\":null,\"organizationalUnitName\":\"Honeywell\"}";
    private static final String defaultOrgUnitPayload = "{\"name\":\"Honeywell\",\"description\":\"Default Connexo organizational unit\",\"owner\":\"admin\",\"defaultGroupId\":\"Honeywell\"}";
    private static final String orgUnitName = "Honeywell";

    public static void main(String args[]) {
        if ((args.length < 4) || (args[0].equals("deployProcess") && (args.length < 5))) {
            System.out.println("Incorrect syntax. The following parameters are required:");
            System.out.println("command -- command identifier");
            System.out.println("user -- a Connexo Flow user with administrative privileges");
            System.out.println("password -- password for the provided user");
            System.out.println("url -- url to the Connexo Flow installation");
            System.out.println("deploymentId -- deployment identifier for deploy process");
            return;
        }

        String authString = "Basic " + new String(Base64.getEncoder().encode((args[1] + ":" + args[2]).getBytes()));
        if (args[0].equals("createRepository")) {
            createRepository(args[3], authString, defaultRepoPayload);
        }
        if (args[0].equals("createOrganizationalUnit")) {
            createOrganizationalUnit(args[3], authString, defaultOrgUnitPayload);
        }
        if (args[0].equals("deployProcess")) {
            deployProcess(args[4], args[3], authString);
        }
    }

    private static void createRepository(String arg, String authString, String payload) {
        String url = arg + "/rest/repositories/";
        doPostAndWait(url, authString, payload);
    }

    private static void createOrganizationalUnit(String arg, String authString, String payload) {
        String url = arg + "/rest/organizationalunits/";
        doPostAndWait(url, authString, payload);
        doVerify(url + orgUnitName, authString);
    }

    private static void deployProcess(String deploymentId, String arg, String authString) {
        String baseUrl = "/rest/deployment/" + deploymentId;
        if (!doGet(baseUrl, authString)) {
            String url = arg + baseUrl + "/deploy?strategy=SINGLETON";
            doPostAndWait(url, authString, null);
        }
    }

    private static boolean doPost(String url, String authString, String payload) {
        int responseCode = 404;
        HttpURLConnection httpConnection = null;
        try {

            URL targetUrl = new URL(url);
            httpConnection = (HttpURLConnection) targetUrl.openConnection();
            httpConnection.setDoOutput(true);
            httpConnection.setRequestMethod("POST");
            httpConnection.setRequestProperty("Authorization", authString);

            if (payload != null && !payload.isEmpty()) {
                httpConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                OutputStreamWriter outWriter = new OutputStreamWriter(httpConnection.getOutputStream(), "UTF-8");
                outWriter.write(payload);
                outWriter.close();
            } else {
                httpConnection.setRequestProperty("Content-Type", "text/plain;charset=UTF-8");
            }

            responseCode = httpConnection.getResponseCode();
            if (responseCode != 202 && responseCode != 404) {
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

        if(responseCode == 404){
            return false;
        }
        return true;
    }

    private static boolean doGet(String url, String authString) {
        HttpURLConnection httpConnection = null;
        try {

            URL targetUrl = new URL(url);
            httpConnection = (HttpURLConnection) targetUrl.openConnection();
            httpConnection.setDoOutput(true);
            httpConnection.setRequestMethod("GET");
            httpConnection.setRequestProperty("Authorization", authString);
            httpConnection.setRequestProperty("Content-Type", "text/plain;charset=UTF-8");

            if (httpConnection.getResponseCode() != 200) {
                return false;
            }

        } catch (IOException e) {
            return false;
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
        return true;
    }

    private static void doVerify(String url, String authString) {
        int maxSteps = 12;
        int timeout = 5 * 1000;

        while ((maxSteps != 0) && (doGet(url, authString) == false)) {
            try {
                maxSteps--;
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

    }

    private static void doPostAndWait(String url, String authString, String payload) {
        int maxSteps = 12;
        int timeout = 5 * 1000;

        while ((maxSteps != 0) && (doPost(url, authString, payload) == false)) {
            try {
                maxSteps--;
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

    }
}
