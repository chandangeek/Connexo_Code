package com.energyict.mdc.common.rest;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.energyict.mdc.common.impl.MessageSeeds;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.QueryParam;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@XmlRootElement
public class JsonQueryFilter {

    private static final String PROPERTY = "property";
    private static final String VALUE = "value";
    private static final BooleanAdapter BOOLEAN_ADAPTER = new BooleanAdapter();
    private static final LongAdapter LONG_ADAPTER = new LongAdapter();
    private static final DateAdapter DATE_ADAPTER = new DateAdapter();

    private final Map<String,Object> filterProperties = new HashMap<>();

    public static JsonQueryFilter unfiltered() {
        return new JsonQueryFilter(null);
    }

    public JsonQueryFilter(@QueryParam("filter") JSONArray filterArray) {
        if (filterArray!=null) {
            for(int i = 0; i < filterArray.length(); i++) {
                try {
                    JSONObject object = filterArray.getJSONObject(i);
                    filterProperties.put(object.getString(PROPERTY), object.get(VALUE));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public Map<String, Object> getFilterProperties() {
        return filterProperties;
    }

    public <T> T getProperty(String name, XmlAdapter<String, T> adapter) {
        String stringProperty = getFilterProperties().get(name)!=null?getFilterProperties().get(name).toString():null;
        try {
            return adapter.unmarshal(stringProperty);
        } catch (Exception e) {
            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_VALUE, name);
        }
    }

    public Date getDate(String name) {
        return getProperty(name, DATE_ADAPTER);
    }

    public Long getLong(String name) {
        return getProperty(name, LONG_ADAPTER);
    }

    public Boolean getBoolean(String name) {
        return getProperty(name, BOOLEAN_ADAPTER);
    }

    public <T> T getProperty(String name) {
        return (T) getFilterProperties().get(name);
    }

    public <T> List<T> getPropertyList(String name, XmlAdapter<String, T> adapter) {
        try {
            JSONArray jsonArray = (JSONArray) getFilterProperties().get(name);
            List<T> values = new ArrayList<>(jsonArray.length());
            for (int i=0; i<jsonArray.length(); i++) {
                values.add(adapter.unmarshal(jsonArray.getString(i)));
            }
            return values;
        } catch (Exception e) {
            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_VALUE, name);
        }
    }

    public List<String> getPropertyList(String name) {
        try {
            JSONArray jsonArray = (JSONArray) getFilterProperties().get(name);
            List<String> values = new ArrayList<>(jsonArray.length());
            for (int i=0; i<jsonArray.length(); i++) {
                values.add(jsonArray.getString(i));
            }
            return values;
        } catch (Exception e) {
            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_VALUE, name);
        }
    }

}
