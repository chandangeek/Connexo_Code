/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.rest;

import com.elster.jupiter.bpm.rest.impl.DateConvertor;
import org.json.JSONException;
import org.json.JSONObject;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class VariableInfo {

    public String variableInstanceId;
    public String value;
    public String date;


    public VariableInfo() {
    }

    public VariableInfo(JSONObject variable) {
        try {
            this.variableInstanceId = variable.getString("variableInstanceId");
            this.value = variable.getString("value");
            this.date = DateConvertor.convertTimeStamps(variable.getString("date"), false);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

}
