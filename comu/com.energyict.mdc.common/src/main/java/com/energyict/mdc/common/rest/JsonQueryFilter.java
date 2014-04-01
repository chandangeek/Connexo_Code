package com.energyict.mdc.common.rest;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.impl.MessageSeeds;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.QueryParam;
import javax.xml.bind.annotation.adapters.XmlAdapter;
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

    public <T> T getProperty(String name, XmlAdapter<String, T> adapter, Thesaurus thesaurus) {
        String stringProperty = getFilterProperties().get(name);
        try {
            return adapter.unmarshal(stringProperty);
        } catch (Exception e) {
            throw new LocalizedFieldValidationException(thesaurus, MessageSeeds.INVALID_VALUE, name);
//            throw new JsonDeserializeException(e, stringProperty,
//                    ((Class<?>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]));
        }
    }
}
