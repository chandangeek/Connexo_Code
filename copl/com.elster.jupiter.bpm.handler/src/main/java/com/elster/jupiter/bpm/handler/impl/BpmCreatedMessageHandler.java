/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.handler.impl;

import com.elster.jupiter.bpm.BpmProcess;
import com.elster.jupiter.bpm.BpmServer;
import com.elster.jupiter.http.whiteboard.HttpAuthenticationService;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.util.json.JsonService;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public class BpmCreatedMessageHandler implements MessageHandler {
    private final JsonService jsonService;
    private final BpmServer bpmRestClient;
    private final HttpAuthenticationService httpAuthenticationService;

    public BpmCreatedMessageHandler(JsonService jsonService, BpmServer server, HttpAuthenticationService httpAuthenticationService) {
        this.jsonService = jsonService;
        this.bpmRestClient = server;
        this.httpAuthenticationService = httpAuthenticationService;
    }


    @Override
    public void process(Message message) {
        BpmProcess bpmProcess =  jsonService.deserialize(message.getPayload(), BpmProcess.class);
        String targetURL = "/rest/runtime/"+bpmProcess.getDeploymentId()+"/process/"+bpmProcess.getId()+"/start"+getProcessParameters(bpmProcess.getParameters());
        bpmRestClient.doPost(targetURL, null, bpmProcess.getAuth() != null ? bpmProcess.getAuth() : httpAuthenticationService.generateTokenForProcessExecution());
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
            result = "?" + result.substring(0, result.length()-1);
        }
        return result;
    }

}