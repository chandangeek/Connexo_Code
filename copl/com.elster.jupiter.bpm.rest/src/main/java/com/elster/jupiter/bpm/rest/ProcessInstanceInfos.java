/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.rest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class ProcessInstanceInfos {
    public int total;

    public List<ProcessInstanceInfo> instances = new ArrayList<>();

    private List<ProcessInstanceInfo> bulkInstances = new ArrayList<>();

    public ProcessInstanceInfos() {
    }

    public ProcessInstanceInfos(JSONArray instances) {
        addAll(instances);
    }

    public ProcessInstanceInfos(JSONArray instances, int limit, int start) {
        this(instances);
        if ( start + limit > bulkInstances.size() ) {
            limit = bulkInstances.size() - start;
        }

        this.instances = bulkInstances.subList(start, start + limit);
        if(total > start + limit){
            total = start + limit + 1;
        }
    }

    void addAll(JSONArray instanceList) {
        if (instanceList != null) {
            for(int i = 0; i < instanceList.length(); i++) {
                try {
                    JSONObject instance = instanceList.getJSONObject(i);
                    ProcessInstanceInfo result = new ProcessInstanceInfo(instance);
                    bulkInstances.add(0, result);
                    total++;
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
