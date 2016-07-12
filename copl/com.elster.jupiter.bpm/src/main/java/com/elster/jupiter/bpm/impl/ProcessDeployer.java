package com.elster.jupiter.bpm.impl;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

/**
 * Main class to deploy the predefined processes during Connexo install
 */
public class ProcessDeployer {

    private static final String defaultRepoPayload="{\"name\":\"connexoRepo\",\"description\":\"repository for the new Connexo project\",\"userName\":null,\"password\":null,\"requestType\":\"new\",\"gitURL\":null}";
    private static final String defaultOrgUnitPayload="{\"name\":\"connexoGroup\",\"description\":\"default Connexo organizational unit\",\"owner\":\"rootUsr\",\"repositories\":[\"connexoRepo\"]}";

    public static void main(String args[]) {
        if ((args.length < 4) || (args[0].equals("deployProcess") && (args.length < 5))) {
            System.out.println("Incorrect syntax. The following parameters are required:");
            System.out.println("command -- command identifier");
            System.out.println("user -- a Conenxo Flow user with administrative privileges");
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

    private static void createRepository(String arg, String authString, String payload){
       String url = arg + "/rest/repositories/";
        doPost(url, authString, payload);
    }

    private static void createOrganizationalUnit(String arg, String authString, String payload){
        String url = arg + "/rest/organizationalunits/";
        doPost(url, authString, payload);
    }

    private static void deployProcess(String deploymentId, String arg, String authString) {
        String url = arg + "/rest/deployment/" + deploymentId + "/deploy?strategy=SINGLETON";
        doPost(url, authString, null);
    }

    private static void doPost(String url, String authString, String payload) {
        HttpURLConnection httpConnection = null;
        try{

        URL targetUrl = new URL(url);
        httpConnection = (HttpURLConnection)targetUrl.openConnection();
            httpConnection.setDoOutput(true);
            httpConnection.setRequestMethod("POST");
            httpConnection.setRequestProperty("Authorization", authString);

            if(payload!=null && payload.length()!= 0) {
                httpConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                OutputStreamWriter outWriter = new OutputStreamWriter(httpConnection.getOutputStream(),"UTF-8");
                outWriter.write(payload);
                outWriter.close();
            }else{
                httpConnection.setRequestProperty("Content-Type", "text/plain;charset=UTF-8");
                }

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
