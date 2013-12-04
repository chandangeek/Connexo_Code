package com.energyict.mdc.rest.impl.filter;

import java.util.List;
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

    public Filter(List<FilteredField> filter) {
        for (FilteredField filteredField : filter) {
            filterProperties.put(filteredField.property, filteredField.value);
        }
    }

    public Map<String, String> getFilterProperties() {
        return filterProperties;
    }
}
