/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.impl;

import com.elster.jupiter.bpm.BpmService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.HashMap;
import java.util.Map;

@Component(name = "com.elster.jupiter.bpm.console", service = {BpmConsoleService.class}, property = {"name=" + "BPM" + ".console", "osgi.command.scope=jupiter", "osgi.command.function=startBpmProcess"}, immediate = true)
public class BpmConsoleService {

    private volatile BpmService bpmService;

    public void startBpmProcess(String deploymentId, String processId, String parameters){
        Map<String, Object> parameterList = getParameters(parameters);
        if (parameterList != null) {
            bpmService.startProcess(deploymentId, processId, parameterList);
        }
    }

    private Map<String, Object> getParameters(String parameters) {
        Map<String, Object> result = new HashMap<>();
        if (parameters != null) {
            String[] params = parameters.split("\\[&&\\]");
            for (String param : params) {
                String[] group = param.split("=");
                if (group.length == 2) {
                    result.put(group[0], group[1]);
                }
                else {
                    System.out.println("Invalid parameter format: deploymentId processId name1=value1&name2=value2& ...");
                    return null;
                }
            }
        }
        return result;
    }

    @Reference
    public void setBpmService(BpmService bpmService) {
        this.bpmService = bpmService;
    }
}
