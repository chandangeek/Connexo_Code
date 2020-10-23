/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.rest.impl;

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
            this.variableInstanceId = variable.getString("process-instance-id");
            this.value = variable.getString("value");
            this.date = DateConvertor.convertTimeStamps(variable.getJSONObject("modification-date").getString("java.util.Date"), false);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

}
