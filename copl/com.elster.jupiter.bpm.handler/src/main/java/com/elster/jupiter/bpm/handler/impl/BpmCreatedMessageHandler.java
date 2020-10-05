/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.handler.impl;

import com.elster.jupiter.bpm.BpmProcess;
import com.elster.jupiter.bpm.BpmServer;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.util.json.JsonService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public class BpmCreatedMessageHandler implements MessageHandler {
    private final JsonService jsonService;
    private final BpmServer bpmRestClient;

    public BpmCreatedMessageHandler(JsonService jsonService, BpmServer server) {
        this.jsonService = jsonService;
        this.bpmRestClient = server;
    }

    @Override
    public void process(Message message) {
        BpmProcess bpmProcess =  jsonService.deserialize(message.getPayload(), BpmProcess.class);
        ObjectMapper mapper=new ObjectMapper();
        String payload=null;
        if(bpmProcess.getParameters()!=null) {
            try {
                payload = mapper.writeValueAsString(bpmProcess.getParameters());
            } catch (JsonProcessingException e) {
            }
        }
        String targetURL = "/services/rest/server/containers/"+bpmProcess.getDeploymentId()+"/processes/"+bpmProcess.getId()+"/instances";
        bpmRestClient.doPost(targetURL, payload, bpmProcess.getAuth());
    }
}