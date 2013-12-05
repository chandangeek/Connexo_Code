package com.energyict.mdc.rest.impl.filter;

import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
                throw new RuntimeException(e);
            }
        }
    }

    public Map<String, String> getFilterProperties() {
        return filterProperties;
    }
}
