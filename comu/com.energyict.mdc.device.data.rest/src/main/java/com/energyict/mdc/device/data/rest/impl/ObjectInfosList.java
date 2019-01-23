package com.energyict.mdc.device.data.rest.impl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ObjectInfosList {
    public int total;

    public List<ObjectInfo> objects = new ArrayList<>();

    public ObjectInfosList() {
    }

    public ObjectInfosList(JSONArray objects) {
        addAll(objects);
    }

    private void addAll(JSONArray processList) {
        if (processList != null) {
            for(int i = 0; i < processList.length(); i++) {
                try {
                    JSONObject task = processList.getJSONObject(i);
                    ObjectInfo result = new ObjectInfo(task);
                    objects.add(result);
                    total++;
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public String getObjectId(int index){
        return objects.get(index).getObjectId();
    }

    public String getObjectType(int index){
        return objects.get(index).getObjectType();
    }

    public void setObjectName(int index,  String nameToSet){
        objects.get(index).setObjectName(nameToSet);
    }


}
