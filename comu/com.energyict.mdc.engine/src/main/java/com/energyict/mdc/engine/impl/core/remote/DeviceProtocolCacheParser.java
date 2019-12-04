package com.energyict.mdc.engine.impl.core.remote;

import com.elster.jupiter.util.MessageSeeds;
import com.energyict.mdc.engine.exceptions.DataAccessException;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;

/**
 * Parses a {@link DeviceProtocolCacheXmlWrapper} JSONObject which is basally a wrapper around a {@link DeviceProtocolCache}.
 *
 */
public class DeviceProtocolCacheParser {

    public DeviceProtocolCache parse(JSONObject queryResult, String propertyName) throws JSONException {
        Object unitJSon = queryResult.get(propertyName);
        return this.parseQueryResult((JSONObject) unitJSon);
    }

    private DeviceProtocolCache parseQueryResult(JSONObject unitJSon) {
        try {
            ObjectMapper mapper = ObjectMapperFactory.newMapper();
            DeviceProtocolCacheXmlWrapper wrapper = mapper.readValue(new StringReader(unitJSon.toString()), DeviceProtocolCacheXmlWrapper.class);
            return wrapper.getDeviceProtocolCache();
        } catch (IOException e) {
            throw new DataAccessException(e, MessageSeeds.JSON_DESERIALIZATION_FAILED);
        }
    }
}