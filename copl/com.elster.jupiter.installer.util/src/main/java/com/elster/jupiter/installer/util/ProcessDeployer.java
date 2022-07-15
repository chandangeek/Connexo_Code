/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.installer.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Base64;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class to deploy the predefined processes during Connexo install
 */
public class ProcessDeployer {
    private static final String LINE = "\r\n";
    private static final String DEFAULT_REPO_PAYLOAD = "{\"name\":\"Connexo\",\"groupId\":\"Honeywell\",\"version\":\"42\",\"description\":\"Repository for Connexo projects\"}";
    private static final String DEFAULT_SPACE_PAYLOAD = "{\"name\":\"Honeywell\",\"description\":\"Default Connexo organizational unit\",\"owner\":\"admin\",\"defaultGroupId\":\"Honeywell\"}";
    private static final String DEFAULT_DEPLOYMENT_PAYLOAD = "'{'\"container-id\":\"{0}\",\"container-name\":\"\"," +
            "\"release-id\":'{'\"group-id\":\"{1}\",\"artifact-id\":\"{2}\",\"version\":\"{3}\"'}'," +
            "\"configuration\":'{'" +
            "\"RULE\":'{'\"org.kie.server.controller.api.model.spec.RuleConfig\":'{'\"pollInterval\":null,\"scannerStatus\":\"STOPPED\"'}' '}'," +
            "\"PROCESS\":'{'\"org.kie.server.controller.api.model.spec.ProcessConfig\":'{'\"runtimeStrategy\":\"SINGLETON\",\"kbase\":\"\",\"ksession\":\"\",\"mergeMode\":\"MERGE_COLLECTIONS\"'}' '}'" +
            " '}'," +
            "\"status\":\"STARTED\"'}'";
    private static final String SPACE_NAME = "Honeywell";
    private static final int MAX_ATTEMPTS = 5;
    private static final int TIMEOUT = 5000;
    private static final Logger logger = Logger.getLogger(ProcessDeployer.class.getName());

    public static void main(String[] args) {
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
            createRepository(args[3], authString, DEFAULT_REPO_PAYLOAD);
        }
        if (args[0].equals("createSpace")) {
            createSpace(args[3], authString, DEFAULT_SPACE_PAYLOAD);
        }
        if (args[0].equals("deployProcess")) {
            deployProcess(args[4], args[3], authString);
        }
    }

    private static void createRepository(String arg, String authString, String payload) {
        String url = arg + "/rest/spaces/"+ SPACE_NAME +"/projects";
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
        doVerify(url + SPACE_NAME, authString);
    }

    private static void deployProcess(String deploymentId, String arg, String authString) {
        String baseUrl = "/rest/controller/management/servers/default-kieserver/containers/" + deploymentId;
        if (!doGetDeployment(arg + baseUrl, authString)) {
            logger.info("Deploying " + deploymentId + "...");
            String[] gav = resolveGAV(deploymentId);
            String payload = MessageFormat.format(DEFAULT_DEPLOYMENT_PAYLOAD, deploymentId, gav[0], gav[1], gav[2]);
            doPutAndWait(arg + baseUrl, authString, payload);
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
                OutputStreamWriter outWriter = new OutputStreamWriter(httpConnection.getOutputStream(), StandardCharsets.UTF_8);
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
            throw new RuntimeException("POST call to Flow REST API failed.", e);
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
        return responseCode != 404;
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
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);

            String[] gav = resolveGAV(kjar);
            addField(writer, boundary, "groupId", gav[0]);
            addField(writer, boundary, "artifactId", gav[1]);
            addField(writer, boundary, "version", gav[2]);
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
            throw new RuntimeException("POST call to Flow REST API failed.", e);
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
        return responseCode != 404;
    }

    private static String[] resolveGAV(File kjar) {
        String groupPath = kjar.getAbsoluteFile().getParentFile().getParentFile().getParentFile().getAbsolutePath();
        String group = groupPath.substring(groupPath.lastIndexOf("kie/") + 4).replace('/', '.');
        String name = kjar.getName();
        String[] artifactAndVersion = name.substring(0, name.lastIndexOf('.')).split("-");
        return new String[]{group, artifactAndVersion[0], artifactAndVersion[1]};
    }

    private static String[] resolveGAV(String deploymentId) {
        return deploymentId.split(":");
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
                OutputStreamWriter outWriter = new OutputStreamWriter(httpConnection.getOutputStream(), StandardCharsets.UTF_8);
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
            throw new RuntimeException("PUT call to Flow REST API failed.", e);
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
        return responseCode != 404;
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
            throw new RuntimeException("GET deployment call to Flow REST API failed.", e);
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
        return true;
    }

    private static String readInputStreamToString(HttpURLConnection connection) {
        String result;
        StringBuilder sb = new StringBuilder();
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
            throw new RuntimeException("Failed reading response from Flow REST API.", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    throw new RuntimeException("Failed closing the connection to Flow REST API.", e);
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
            throw new RuntimeException("GET call to Flow REST API failed.", e);
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
        return true;
    }

    private static void doVerify(String url, String authString) {
        int remainingAttempts = MAX_ATTEMPTS;
        boolean result = false;
        while (remainingAttempts != 0 && !result) {
            try {
                remainingAttempts--;
                Thread.sleep(TIMEOUT);
                result = doGet(url, authString);
                logger.info("successfully verified: " + result);
            } catch (RuntimeException e) {
                if (remainingAttempts == 0) {
                    throw e;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static void doPostAndWait(String url, String authString, String payload) {
        int remainingAttempts = MAX_ATTEMPTS;
        boolean result = false;
        while (remainingAttempts != 0 && !result) {
            try {
                remainingAttempts--;
                logger.info("try " + (MAX_ATTEMPTS - remainingAttempts));
                Thread.sleep(TIMEOUT);
                result = doPost(url, authString, payload);
                logger.info("success: " + result);
            } catch (RuntimeException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
                if (remainingAttempts == 0) {
                    throw e;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static void doPostAndWait(String url, String authString, File f) {
        int remainingAttempts = MAX_ATTEMPTS;
        boolean result = false;
        while (remainingAttempts != 0 && !result) {
            try {
                remainingAttempts--;
                logger.info("try " + (MAX_ATTEMPTS - remainingAttempts));
                Thread.sleep(TIMEOUT);
                result = doPost(url, authString, f);
                logger.info("success: " + result);
            } catch (RuntimeException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
                if (remainingAttempts == 0) {
                    throw e;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static void doPutAndWait(String url, String authString, String payload) {
        int remainingAttempts = MAX_ATTEMPTS;
        boolean result = false;
        while (remainingAttempts != 0 && !result) {
            try {
                remainingAttempts--;
                logger.info("try " + (MAX_ATTEMPTS - remainingAttempts));
                Thread.sleep(TIMEOUT);
                result = doPUT(url, authString, payload);
                logger.info("success: " + result);
            } catch (RuntimeException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
                if (remainingAttempts == 0) {
                    throw e;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
