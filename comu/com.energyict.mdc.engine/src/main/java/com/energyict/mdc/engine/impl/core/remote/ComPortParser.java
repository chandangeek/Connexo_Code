package com.energyict.mdc.engine.impl.core.remote;

import com.energyict.mdc.engine.impl.core.RemoteComServerQueryJSonPropertyNames;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Parses a JSONObject that was returned by a remote query to a {@link ComPort}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-11 (09:52)
 */
public class ComPortParser {

    private final EngineConfigurationService engineConfigurationService;

    public ComPortParser(EngineConfigurationService engineConfigurationService) {
        this.engineConfigurationService = engineConfigurationService;
    }

    public ComPort parse (JSONObject queryResult) throws JSONException {
        Object comPortJSon = queryResult.get(RemoteComServerQueryJSonPropertyNames.SINGLE_OBJECT_RESULT);
        return engineConfigurationService.parseComPortQueryResult((JSONObject) comPortJSon);
    }
}