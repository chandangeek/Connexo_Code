package com.energyict.mdc.common.rest;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.QueryParam;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonQueryFilter {
    private static final String PROPERTY = "property";
    private static final String VALUE = "value";

    private final Map<String,String> filterProperties = new HashMap<>();

    public static JsonQueryFilter unfiltered() {
        return new JsonQueryFilter(null);
    }

    public JsonQueryFilter(@QueryParam("filter") JSONArray filterArray) {
        if (filterArray!=null) {
            for(int i = 0; i < filterArray.length(); i++) {
                try {
                    JSONObject object = filterArray.getJSONObject(i);
                    filterProperties.put(object.getString(PROPERTY), object.getString(VALUE));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public Map<String, String> getFilterProperties() {
        return filterProperties;
    }
}
