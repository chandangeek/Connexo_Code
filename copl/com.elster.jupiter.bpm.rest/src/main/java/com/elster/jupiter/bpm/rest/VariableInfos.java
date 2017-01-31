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
public class VariableInfos {
    public int total;

    public List<VariableInfo> variables = new ArrayList<>();

    public VariableInfos() {
    }

    public VariableInfos(JSONArray variables) {
        addAll(variables);
    }

    void addAll(JSONArray variableList) {
        if (variableList != null) {
            for(int i = 0; i < variableList.length(); i++) {
                try {
                    JSONObject variable = variableList.getJSONObject(i);
                    VariableInfo result = new VariableInfo(variable);
                    variables.add(result);
                    total++;
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
