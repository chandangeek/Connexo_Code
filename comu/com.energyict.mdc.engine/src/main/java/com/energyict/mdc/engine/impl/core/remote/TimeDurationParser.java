package com.energyict.mdc.engine.impl.core.remote;

import com.energyict.mdc.engine.exceptions.DataAccessException;
import com.energyict.mdc.engine.impl.MessageSeeds;
import com.energyict.mdc.engine.impl.core.RemoteComServerQueryJSonPropertyNames;

import com.elster.jupiter.time.TimeDuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;

/**
 * Parses a JSONObject that was returned by a remote query to a {@link com.energyict.mdc.engine.config.ComPort}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-11 (09:52)
 */
public class TimeDurationParser {

    public TimeDuration parse (JSONObject queryResult) throws JSONException {
        Object comServerJSon = queryResult.get(RemoteComServerQueryJSonPropertyNames.SINGLE_OBJECT_RESULT);
        return this.parseQueryResult((JSONObject) comServerJSon);
    }

    private TimeDuration parseQueryResult (JSONObject timeDurationJSon) {
        try {
            ObjectMapper mapper = ObjectMapperFactory.newMapper();
            TimeDurationXmlWrapper wrapper = mapper.readValue(new StringReader(timeDurationJSon.toString()), TimeDurationXmlWrapper.class);
            return wrapper.timeDuration;
        }
        catch (IOException e) {
            throw new DataAccessException(e, MessageSeeds.UNEXPECTED_SQL_ERROR);
        }
    }

}