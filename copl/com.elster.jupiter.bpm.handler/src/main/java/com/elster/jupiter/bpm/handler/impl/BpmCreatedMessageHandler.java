/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.handler.impl;

import com.elster.jupiter.bpm.BpmProcess;
import com.elster.jupiter.bpm.BpmServer;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.bpm.HttpException;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.util.Version;
import com.elster.jupiter.util.json.JsonService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.NoSuchElementException;

public class BpmCreatedMessageHandler implements MessageHandler {
    private final JsonService jsonService;
    private final BpmServer bpmRestClient;
    private final BpmService bpmService;

    @Inject
    public BpmCreatedMessageHandler(JsonService jsonService, BpmService bpmService) {
        this.jsonService = jsonService;
        this.bpmRestClient = bpmService.getBpmServer();
        this.bpmService = bpmService;
    }

    @Override
    public void process(Message message) {
        BpmProcess bpmProcess = jsonService.deserialize(message.getPayload(), BpmProcess.class);
        String targetURL;

        if (bpmProcess.getDeploymentId().isPresent() && bpmProcess.getId().isPresent()) {
            targetURL = "/rest/runtime/" + bpmProcess.getDeploymentId().get() + "/process/" + bpmProcess.getId().get() + "/start" + getProcessParameters(bpmProcess.getParameters());
        } else if (bpmProcess.getName().isPresent() && bpmProcess.getVersion().isPresent()) {
            try {
                BpmProcess latestProcess = getProcessFromLatestDeployment(bpmProcess);
                targetURL = "/rest/runtime/" + latestProcess.getDeploymentId().get() + "/process/" + latestProcess.getId().get()
                        + "/start" + getProcessParameters(bpmProcess.getParameters());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("Neither deployment id / process id nor name / version are provided for the process to start.");
        }

        try {
            bpmRestClient.doPost(targetURL, null, bpmProcess.getAuth().orElse(null));
        } catch (HttpException httpException) {
            if (httpException.getResponseCode() == 404) {
                findAndRunProcessWithHighestDeploymentVersion(bpmProcess);
            } else {
                throw httpException;
            }
        }
    }

    private void findAndRunProcessWithHighestDeploymentVersion(BpmProcess bpmProcess) {
        try {
            BpmProcess latestProcess = getProcessFromLatestDeployment(bpmProcess);
            bpmService.startProcess(latestProcess.getDeploymentId().get(), latestProcess.getId().get(), latestProcess.getParameters(), latestProcess.getAuth().orElse(null));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private BpmProcess getProcessFromLatestDeployment(BpmProcess bpmProcess) throws JSONException {
        JSONArray processArr = getDeployedProcesses(bpmProcess.getAuth().orElse(null));
        Version resultDeploymentVersion = Version.EMPTY;
        JSONObject resultProcessDefinition = null;
        for (int i = 0; i < processArr.length(); i++) {
            JSONObject item = processArr.getJSONObject(i);
            if (bpmProcess.getId().filter(item.getString("id")::equals).isPresent()
                    || bpmProcess.getName().filter(item.getString("name")::equals).isPresent()
                    && bpmProcess.getVersion().filter(item.getString("version")::equals).isPresent()) {
                String deploymentVersion = getDeploymentVersionFromDeploymentId(item.getString("deploymentId"));
                Version itemVersion = Version.fromString(deploymentVersion);
                if (itemVersion.compareTo(resultDeploymentVersion) > 0) {
                    resultDeploymentVersion = itemVersion;
                    resultProcessDefinition = item;
                }
            }
        }
        if (resultProcessDefinition != null) {
            return BpmProcess.identifiedByDeploymentIdAndId(resultProcessDefinition.getString("deploymentId"),
                    resultProcessDefinition.getString("id"),
                    bpmProcess.getParameters(),
                    bpmProcess.getAuth().orElse(null));
        } else {
            if (bpmProcess.getId().isPresent()) {
                throw new NoSuchElementException("Couldn't find any process definition with id " + bpmProcess.getId().get() + ".");
            } else {
                throw new NoSuchElementException("Couldn't find any process definition with name " + bpmProcess.getName().orElse(null)
                        + " and version " + bpmProcess.getVersion().orElse(null) + '.');
            }
        }
    }

    private JSONArray getDeployedProcesses(String authorization) {
        String jsonContent;
        JSONArray arr = null;
        try {
            jsonContent = bpmRestClient.doGet("/rest/deployment/processes?p=0&s=1000", authorization);
            if (!"".equals(jsonContent)) {
                JSONObject responseJsonObject = new JSONObject(jsonContent);
                arr = responseJsonObject.getJSONArray("processDefinitionList");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        if (arr == null) {
            throw new RuntimeException("Process definitions aren't available.");
        }
        return arr;
    }

    private String getDeploymentVersionFromDeploymentId(String deploymentId) {
        return deploymentId.substring(deploymentId.lastIndexOf(":"));
    }

    private String getProcessParameters(Map<String, Object> params) {
        String result = "";
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String paramValue = "&";
                try {
                    if (entry.getValue() != null) {
                        paramValue = URLEncoder.encode(entry.getValue().toString(), "UTF-8") + "&";
                    }
                } catch (UnsupportedEncodingException e) {
                    throw new IllegalArgumentException(e);
                }
                result += "map_" + entry.getKey() + "=" + paramValue;
            }
            result = "?" + result.substring(0, result.length() - 1);
        }
        return result;
    }

}
