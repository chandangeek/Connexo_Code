package com.energyict.mdc.device.data.rest.impl;

import org.json.JSONException;
import org.json.JSONObject;

public class ObjectInfo {
    public String type;
    public String objectId;
    public String name;


    public ObjectInfo(){

    }

    public ObjectInfo(JSONObject jsonObject) {
        try {
            this.type = jsonObject.getString("variableId");
            this.objectId = jsonObject.getString("variableValue");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void setObjectName(String nameToSet){
        this.name = nameToSet;
    }

    public String getObjectType(){
        return type;
    }

    public String getObjectId(){
        return objectId;
    }


    @Override
    public String toString(){
        return "name="+this.name+"objectId"+this.objectId;
    }
}
