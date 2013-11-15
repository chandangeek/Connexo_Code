package com.energyict.mdc.rest.impl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Filter {
    public static final String PROPERTY = "property";
    public static final String VALUE = "value";
    private Map<String,String> filterProperties = new HashMap<>();
    public Filter(JSONArray filterArray) {
        for(int i = 0; i < filterArray.length(); i++)
        {
            try {
                JSONObject object = filterArray.getJSONObject(i);
                filterProperties.put(object.getString(PROPERTY), object.getString(VALUE));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public Map<String, String> getFilterProperties() {
        return filterProperties;
    }
}
