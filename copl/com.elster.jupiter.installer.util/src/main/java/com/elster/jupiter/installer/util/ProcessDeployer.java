/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.installer.util;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;
import java.util.Collections;
import java.util.UUID;

/**
 * Main class to deploy the predefined processes during Connexo install
 */
public class ProcessDeployer {


    final static String LINE = "\r\n";
    private static final String defaultRepoPayload = "{\"name\":\"Connexo\",\"groupId\":\"Honeywell\",\"version\":\"2.8.1\",\"description\":\"Repository for Connexo projects\"}";
    private static final String defaultSpacePayload = "{\"name\":\"Honeywell\",\"description\":\"Default Connexo organizational unit\",\"owner\":\"admin\",\"defaultGroupId\":\"Honeywell\"}";
    private static final String defaultdeployPayload = "{\r\n \"release-id\":{\r\n \"group-id\":\"com.energyict\",\r\n \"artifact-id\":\"DeviceProcesses\",\r\n \"version\":\"2.8.1\"\r\n }\r\n}";
    private static final String spaceName = "Honeywell";

    public static void main(String args[]) {
        if ("installProcesses".equals(args[0])) {
            if (args.length != 5) {
                System.out.println("Incorrect syntax. The following parameters are required:");
                System.out.println("command -- command identifier");
                System.out.println("user -- a Connexo Flow user with administrative privileges");
                System.out.println("password -- password for the provided user");
                System.out.println("url -- url to the Connexo Flow installation");
                System.out.println("file -- kjar to install");
            } else {
                String authString = "Basic " + new String(Base64.getEncoder().encode((args[1] + ":" + args[2]).getBytes()));
                installProcesses(args[3], args[4], authString);
            }
            return;
        } else if ((args.length < 4) || (args[0].equals("deployProcess") && (args.length < 5))) {
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
        if (args[0].equals("createSpace")) {
            createSpace(args[3], authString, defaultSpacePayload);
        }
        if (args[0].equals("deployProcess")) {
            deployProcess(args[4], args[3], authString);
        }
    }

    private static void createRepository(String arg, String authString, String payload) {
        String url = arg + "/rest/spaces/"+spaceName+"/projects";
        doPostAndWait(url, authString, payload);
    }

    private static void installProcesses(String arg, String filePath, String authString) {
        String url = arg + "/maven2";
        File file = new File(filePath);
        doPostAndWait(url, authString, file);
    }

    private static void createSpace(String arg, String authString, String payload) {
        String url = arg + "/rest/spaces/";
        doPostAndWait(url, authString, payload);
        doVerify(url + spaceName, authString);
    }

    private static void deployProcess(String deploymentId, String arg, String authString) {
        String baseUrl = "/services/rest/server/containers/" + deploymentId;
        if (!doGetDeployment(arg + baseUrl, authString)) {
            String deployUrl = arg + baseUrl;
            doPutAndWait(deployUrl, authString, defaultdeployPayload);
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
                throw new RuntimeException("Failed POST on " + url + ": HTTP error code : "
                        + httpConnection.getResponseCode());
            }

        } catch (IOException e) {
            throw new RuntimeException("POST call to Connexo REST API failed.", e);
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }

        if (responseCode == 404) {
            return false;
        }
        return true;
    }

    private static boolean doPost(String url, String authString, File kjar) {
        int responseCode = 404;
        HttpURLConnection httpConnection = null;
        try {
            String boundary = UUID.randomUUID().toString();
            URL targetUrl = new URL(url);
            httpConnection = (HttpURLConnection) targetUrl.openConnection();
            httpConnection.setDoOutput(true);
            httpConnection.setRequestMethod("POST");
            httpConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            httpConnection.setRequestProperty("Authorization", authString);


            OutputStream outputStream = httpConnection.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);

            addField(writer, boundary,"groupId", "com.energyict");
            addField(writer, boundary,"artifactId", "DeviceProcesses");
            addField(writer, boundary,"version", "2.8.1");
            addFile(writer, outputStream, boundary, "fileUploadElement", kjar);

            writer.flush();
            writer.append("--" + boundary + "--").append(LINE);
            writer.close();

            responseCode = httpConnection.getResponseCode();
            if (!(responseCode == 202 || responseCode == 200) && responseCode != 404) {
                throw new RuntimeException("Failed POST on " + url + ": HTTP error code : "
                        + responseCode);
            }

        } catch (IOException e) {
            throw new RuntimeException("POST call to Connexo REST API failed.", e);
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }

        if (responseCode == 404) {
            return false;
        }
        return true;
    }

    private static void addField(PrintWriter writer, String boundary, String name, String value) {
        writer.append("--" + boundary).append(LINE);
        writer.append("Content-Disposition: form-data; name=\"" + name + "\"").append(LINE);
        writer.append("Content-Type: text/plain; charset=" + "UTF-8").append(LINE);
        writer.append(LINE);
        writer.append(value).append(LINE);
        writer.flush();
    }

    private static void addFile(PrintWriter writer, OutputStream outputStream, String boundary, String fieldName, File uploadFile)
            throws IOException {
        String fileName = uploadFile.getName();
        writer.append("--" + boundary).append(LINE);
        writer.append("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"").append(LINE);
        writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(fileName)).append(LINE);
        writer.append("Content-Transfer-Encoding: binary").append(LINE);
        writer.append(LINE);
        writer.flush();

        FileInputStream inputStream = new FileInputStream(uploadFile);
        byte[] buffer = new byte[4096];
        int bytesRead = -1;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.flush();
        inputStream.close();
        writer.append(LINE);
        writer.flush();
    }

    private static boolean doPUT(String url, String authString, String payload) {
        int responseCode = 404;
        HttpURLConnection httpConnection = null;
        try {

            URL targetUrl = new URL(url);
            httpConnection = (HttpURLConnection) targetUrl.openConnection();
            httpConnection.setDoOutput(true);
            httpConnection.setRequestMethod("PUT");
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
            if (responseCode != 201 && responseCode != 404) {
                throw new RuntimeException("Failed PUT on " + url + ": HTTP error code : "
                        + httpConnection.getResponseCode());
            }

        } catch (IOException e) {
            throw new RuntimeException("PUT call to Connexo REST API failed.", e);
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }

        if (responseCode == 404) {
            return false;
        }
        return true;
    }

    private static boolean doGetDeployment(String url, String authString) {
        HttpURLConnection httpConnection = null;
        try {
            URL targetUrl = new URL(url);
            httpConnection = (HttpURLConnection) targetUrl.openConnection();
            httpConnection.setDoOutput(true);
            httpConnection.setRequestMethod("GET");
            httpConnection.setRequestProperty("Authorization", authString);
            httpConnection.setRequestProperty("Content-Type", "application/json");

            if (httpConnection.getResponseCode() != 200) {
                return false;
            }

            String result = readInputStreamToString(httpConnection);

            if (result.contains("\"type\" : \"FAILURE\"")) {
                return false;
            }

        } catch (IOException e) {
            throw new RuntimeException("GET deployment call to Connexo REST API failed.", e);
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
        return true;
    }

    private static String readInputStreamToString(HttpURLConnection connection) {
        String result = null;
        StringBuffer sb = new StringBuffer();
        InputStream is = null;

        try {
            is = new BufferedInputStream(connection.getInputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String inputLine = "";
            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine);
            }
            result = sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed reading response from Connexo REST API.", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    throw new RuntimeException("Failed closing the connection to Connexo REST API.", e);
                }
            }
        }

        return result;
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
            throw new RuntimeException("GET call to Connexo REST API failed.", e);
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

        boolean result = false;
        while ((maxSteps != 0) && (result == false)) {
            try {
                maxSteps--;
                Thread.sleep(timeout);
                result = doGet(url, authString);
            } catch (RuntimeException e) {
                if (maxSteps == 0) {
                    throw e;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

    }

    private static void doPostAndWait(String url, String authString, String payload) {
        int maxSteps = 12;
        int timeout = 5 * 1000;

        boolean result = false;
        while ((maxSteps != 0) && (result == false)) {
            try {
                maxSteps--;
                Thread.sleep(timeout);
                result = doPost(url, authString, payload);
            } catch (RuntimeException e) {
                if (maxSteps == 0) {
                    throw e;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

    }

    private static void doPostAndWait(String url, String authString, File f) {
        int maxSteps = 12;
        int timeout = 5 * 1000;

        boolean result = false;
        while ((maxSteps != 0) && (result == false)) {
            try {
                maxSteps--;
                Thread.sleep(timeout);
                result = doPost(url, authString, f);
            } catch (RuntimeException e) {
                if (maxSteps == 0) {
                    throw e;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

    }
    private static void doPutAndWait(String url, String authString, String payload) {
        int maxSteps = 12;
        int timeout = 5 * 1000;

        boolean result = false;
        while ((maxSteps != 0) && (result == false)) {
            try {
                maxSteps--;
                Thread.sleep(timeout);
                result = doPUT(url, authString, payload);
            } catch (RuntimeException e) {
                if (maxSteps == 0) {
                    throw e;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

    }
}
