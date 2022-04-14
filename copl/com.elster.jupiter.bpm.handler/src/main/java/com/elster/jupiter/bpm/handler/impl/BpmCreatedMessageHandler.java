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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
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
        ObjectMapper mapper = new ObjectMapper();
        String payload = null;
        if (bpmProcess.getParameters() != null) {
            try {
                payload = mapper.writeValueAsString(bpmProcess.getParameters());
            } catch (JsonProcessingException e) {
                // null payload
            }
        }
        String targetURL;
        if (bpmProcess.getDeploymentId().isPresent() && bpmProcess.getId().isPresent()) {
            targetURL = "/services/rest/server/containers/" + bpmProcess.getDeploymentId().get() + "/processes/" + bpmProcess.getId().get() + "/instances";
        } else if (bpmProcess.getName().isPresent() && bpmProcess.getVersion().isPresent()) {
            try {
                BpmProcess latestProcess = getProcessFromLatestDeployment(bpmProcess);
                targetURL = "/services/rest/server/containers/" + latestProcess.getDeploymentId().get() + "/processes/" + latestProcess.getId().get() + "/instances";
            } catch (JSONException e) {
                LOGGER.log(Level.SEVERE, "CONM-2435:BpmCreatedMessageHandler :: process" + e.getMessage(), e );
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("Neither deployment id / process id nor name / version are provided for the process to start.");
        }

        try {
            bpmRestClient.doPost(targetURL, payload, bpmProcess.getAuth().orElse(null));
        } catch (HttpException httpException) {
            int responseCode = httpException.getResponseCode();
            if (responseCode == 404 || responseCode == 500 || responseCode == 400) {
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
            if (bpmProcess.getId().filter(item.getString("process-id")::equals).isPresent()
                    || bpmProcess.getName().filter(item.getString("process-name")::equals).isPresent()
                    && bpmProcess.getVersion().filter(item.getString("process-version")::equals).isPresent()) {
                String deploymentVersion = getDeploymentVersionFromDeploymentId(item.getString("container-id"));
                Version itemVersion = Version.fromString(deploymentVersion);
                if (itemVersion.compareTo(resultDeploymentVersion) > 0) {
                    resultDeploymentVersion = itemVersion;
                    resultProcessDefinition = item;
                }
            }
        }
        if (resultProcessDefinition != null) {
            return BpmProcess.identifiedByDeploymentIdAndId(resultProcessDefinition.getString("container-id"),
                    resultProcessDefinition.getString("process-id"),
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
            jsonContent = bpmRestClient.doGet("/services/rest/server/queries/processes/definitions?page=0&pageSize=1000", authorization);
            if (!"".equals(jsonContent)) {
                JSONObject responseJsonObject = new JSONObject(jsonContent);
                arr = responseJsonObject.getJSONArray("processes");
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
}
