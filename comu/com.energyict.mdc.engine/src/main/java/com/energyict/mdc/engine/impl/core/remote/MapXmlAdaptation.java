package com.energyict.mdc.engine.impl.core.remote;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.xml.bind.annotation.XmlElement;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Adapter class for {@link  HashMap} to enable xml marshalling.</p>
 * <p/>
 * <p>When using the default JSON {@link com.fasterxml.jackson.databind.ser.std.MapSerializer MapSerializer} to marshall a HashMap
 * then all map keys should be of simple type (preferable String), cause they are marshalled as their toString().
 * This XmlAdapation is specifically written to allow complex objects as key.</p>
 * <p/>
 * <b>Warning:</b> The elements of the map (both the keys as well as the values) should be of a root type
 * (and thus having XlmElement 'type'), or else (de)marshalling will resolve to simple object types instead.
 *
 * @author sva
 * @since 24/07/2014 - 15:31
 */
public class MapXmlAdaptation {

    private static final String TYPE = "type";

    /**
     * A helper HashMap, in which both key and value are of simple type String.
     * It is this hashMap that is JSON marshalled and send over.
     * <p/>
     * The map contains as key the marshalled JSON version of the key object.
     * The map contains as value the marshalled JSON version of the value object.
     */
    @XmlElement
    public HashMap<String, String> marshallableMap;

    /**
     * The ObjectMapper used for (de)marshalling
     */
    private ObjectMapper objectMapper;

    public MapXmlAdaptation() {
        super();
    }

    public MapXmlAdaptation(Map<Object, Object> map) throws IOException {
        this();
        this.marshallableMap = new HashMap<>(map.size());
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            marshallableMap.put(marshallObject(entry.getKey()), marshallObject(entry.getValue()));
        }
    }

    public Map<Object, Object> unmarshallHashMap() throws JSONException {
        if (marshallableMap != null) {
            HashMap<Object, Object> resultMap = new HashMap<>(marshallableMap.size());
            for (Map.Entry<String, String> entry : marshallableMap.entrySet()) {
                resultMap.put(unmarshallJSONObjectOrArray(entry.getKey()), unmarshallJSONObjectOrArray(entry.getValue()));
            }
            return resultMap;
        }
        return null;
    }

    private String marshallObject(Object obj) throws IOException {
        StringWriter writer = new StringWriter();
        getObjectMapper().writeValue(writer, obj);
        return writer.toString();
    }

    @SuppressWarnings("unchecked")
    private Object unmarshallJSONObjectOrArray(String objectJSON) throws JSONException {
        if (objectJSON.startsWith("[")) { // The objectJSON contains a JSONArray
            JSONArray jsonArray = new JSONArray(objectJSON);
            List resultList = new ArrayList(jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                resultList.add(unmarshallJSONObject(jsonArray.get(i).toString()));
            }
            return resultList;
        } else { // The objectJSOn contains a JSONObject
            return unmarshallJSONObject(objectJSON);
        }
    }

    private Object unmarshallJSONObject(String jsonObject) throws JSONException {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        try {
            try {
                Class objectClass = getClassFor(new JSONObject(jsonObject));
                return getObjectMapper().readValue(new StringReader(jsonObject), objectClass);
            } catch (JSONException e) {
                // Thrown if no JSONObject could be made from the given string, in such case try to parse as generic object (e.g. simple boolean / String / ...)
                return getObjectMapper().readValue(new StringReader(jsonObject), Object.class);
            }
        } catch (ClassNotFoundException | IOException e) {
            throw new JSONException((e));
        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }
    }

    private Class getClassFor(JSONObject jsonObject) throws JSONException, ClassNotFoundException {
        return Class.forName(jsonObject.getString(TYPE));
    }

    private ObjectMapper getObjectMapper() {
        if (this.objectMapper == null) {
            objectMapper = ObjectMapperFactory.getObjectMapper();
        }
        return this.objectMapper;
    }
}