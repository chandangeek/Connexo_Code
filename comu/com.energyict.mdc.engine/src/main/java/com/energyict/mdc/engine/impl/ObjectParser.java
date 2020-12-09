package com.energyict.mdc.engine.impl;

import com.energyict.mdc.engine.JSONTypeMapperProvider;
import com.energyict.mdc.engine.exceptions.DataAccessException;
import com.energyict.mdc.engine.impl.core.RemoteComServerQueryJSonPropertyNames;
import com.energyict.mdc.engine.impl.core.remote.ObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;

/**
 * <p>This class contains a generic JSON parser who can be used to parse a JSONObject/JSONArray,
 * read in the actual object/array of objects (specified to be of generic type T)
 * and construct a proper object/array of objects (again of generic type T) out of it.</p>
 * <b>Example usage:</b>
 * <ul>
 * <li>JSONObject parser > new JSONObjectParser&#60;String&#62;</li>
 * <li>JSONArray parser  >> new JSONObjectParser&#60;String[]&#62;</li>
 * </ul>
 *
 * @author sva
 * @since 2/04/2014 - 10:33
 */
public class ObjectParser<T> {

    private static final String NULL_OBJECT = "null";

    /**
     * Use this to parse a single object. The object type should be included in the XML.
     */
    public T parseObject(JSONObject jsonObject, String propertyName) throws JSONException {
        return parseObject(jsonObject, propertyName, null);
    }

    /**
     * Use this to parse a single object. The object type should be included in the XML.
     */
    public T parseObject(JSONObject jsonObject, String propertyName, Class interfaceClazz) throws JSONException {
        if (jsonObject.has(propertyName) && !jsonObject.get(propertyName).toString().equals(NULL_OBJECT)) {
            return this.parseQueryResult(jsonObject.get(propertyName), interfaceClazz);
        } else {
            return null;        //Null object does either not contain property 'single-value' or has value 'null'
        }
    }

    /**
     * Use this to parse an array of items. The array type is included as a parameter (interfaceClazz).
     * Sub elements can be of this type, or any kind of subtype that extends/implements this.
     *
     * @param interfaceClazz the generic type of the array
     */
    public T parseArray(JSONObject jsonObject, String propertyName, Class interfaceClazz) throws JSONException {
        return this.parseQueryResult(jsonObject.get(propertyName), interfaceClazz);
    }

    /**
     * Parse a JSONArray (JSON object that represents an array)
     */
    public T parseArray(JSONArray jsonArray, Class clazz) throws JSONException {
        T array = this.parseQueryResult(jsonArray, clazz);
        return array;
    }

    @SuppressWarnings("unchecked")
    private T parseQueryResult(Object objectJSON, Class interfaceClazz) {
        try {
            convertAllClassNamesFor(objectJSON);
            Class<? extends T> objectClass = interfaceClazz != null ? interfaceClazz : this.getClassFor(objectJSON); // When clazz is null, then extract the type from the objectJSON
            ObjectMapper mapper = ObjectMapperFactory.getObjectMapper();
            T object = mapper.readValue(new StringReader(objectJSON.toString()), objectClass);
            return object;
        } catch (IOException e) {
            throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
        } catch (JSONException e) {
            throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
        }
    }

    private void convertAllClassNamesFor(Object objectJSON) throws JSONException {
        try {
            JSONTypeMapperProvider.instance.get().getJSONTypeMapper().convertAllClassNamesFor(objectJSON);
        } catch (ClassNotFoundException | ClassCastException e) {
            throw new JSONException("The object returned by the remote query API contains one or more invalid object references: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Class<? extends T> getClassFor(Object objectJSON) throws JSONException {
        String xmlType = "Unknown";
        try {
            if (objectJSON instanceof JSONObject) {
                xmlType = ((JSONObject) objectJSON).getString(RemoteComServerQueryJSonPropertyNames.TYPE);
                if (xmlType == null)
                    xmlType = ((JSONObject) objectJSON).getString(RemoteComServerQueryJSonPropertyNames.XML_TYPE);
                return (Class<? extends T>) Class.forName(xmlType);
            } else {
                throw new JSONException("The response returned by the remote query API is not of a known type");
            }
        } catch (ClassNotFoundException | ClassCastException e) {
            throw new JSONException("The object returned by the remote query API is not of a known type, but was " + xmlType);
        }
    }
}