/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.data.rest.impl;

import org.json.JSONException;
import org.json.JSONObject;

public class ProcessInfoForFilter {

    public String name;
    public String processId;
    public String version;
    public String deploymentId;

    public ProcessInfoForFilter(){

    }

    public ProcessInfoForFilter(JSONObject jsonObject) {
        try {
            this.name = jsonObject.getString("process-name");
            this.processId = jsonObject.getString("process-id");
            this.version = jsonObject.getString("process-version");
            this.deploymentId = jsonObject.getString("container-id");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString(){
        return "name="+this.name+"processId"+this.processId+"version="+this.version+"deploymentId"+this.deploymentId;
   }
}
