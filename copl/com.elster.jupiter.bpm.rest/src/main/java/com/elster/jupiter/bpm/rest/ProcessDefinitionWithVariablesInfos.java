/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.rest;


import com.elster.jupiter.bpm.BpmProcessDefinition;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProcessDefinitionWithVariablesInfos {

    public int total;

    public List<ProcessDefinitionWithVariablesInfo> processes = new ArrayList<>();

    public ProcessDefinitionWithVariablesInfos() {
    }

    public ProcessDefinitionWithVariablesInfos(JSONArray processes) {
        addAll(processes);
    }

    public ProcessDefinitionWithVariablesInfos(Iterable<? extends BpmProcessDefinition> bpmProcessDefinitions) {
        addAll(bpmProcessDefinitions);
    }

    void addAll(JSONArray processList) {
        if (processList != null) {
            for (int i = 0; i < processList.length(); i++) {
                try {
                    JSONObject task = processList.getJSONObject(i);
                    ProcessDefinitionWithVariablesInfo result = new ProcessDefinitionWithVariablesInfo(task);
                    processes.add(result);
                    total++;
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    void addAll(Iterable<? extends BpmProcessDefinition> infos) {
        for (BpmProcessDefinition each : infos) {
            add(each);
        }
    }

    public ProcessDefinitionWithVariablesInfo add(BpmProcessDefinition bpmProcessDefinition) {
        ProcessDefinitionWithVariablesInfo processDefinitionInfo = new ProcessDefinitionWithVariablesInfo(bpmProcessDefinition);
        processes.add(processDefinitionInfo);
        total++;
        return processDefinitionInfo;
    }

    public List<ProcessDefinitionWithVariablesInfo> getProcesses() {
        return processes;
    }
}
