package com.elster.jupiter.bpm.handler;

import com.elster.jupiter.bpm.BpmProcess;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.util.json.JsonService;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public class BpmCreatedMessageHandler implements MessageHandler{
    private final JsonService jsonService;

    public BpmCreatedMessageHandler(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Override
    public void process(Message message) {
        BpmProcess bpmProcess =  jsonService.deserialize(message.getPayload(), BpmProcess.class);
        String targetURL = "/rest/runtime/"+bpmProcess.getDeploymentId()+"/process/"+bpmProcess.getId()+"/start"+getProcessParameters(bpmProcess.getParameters());
        new BpmRestClient().doPost(targetURL);
    }

    private String getProcessParameters(Map<String, Object> params) {
        String result = "";
        if (params != null && params.size() > 0) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String paramValue = "&";
                try {
                    paramValue = URLEncoder.encode(entry.getValue().toString(), "UTF-8") +"&";
                } catch (UnsupportedEncodingException e) {
                }
                result += "map_" + entry.getKey() + "=" + paramValue;
            }
            result = "?" + result.substring(0, result.length()-1);
        }
        return result;
    }
}

