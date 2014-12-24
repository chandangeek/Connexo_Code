package com.energyict.mdc.engine.impl.core.remote;

import com.energyict.mdc.engine.impl.core.RemoteComServerQueryJSonPropertyNames;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Parses a JSONObject that was returned by a remote query to a {@link ComServer}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-11 (09:52)
 */
public class ComServerParser {

    private final EngineConfigurationService engineConfigurationService;

    public ComServerParser(EngineConfigurationService engineConfigurationService) {
        this.engineConfigurationService = engineConfigurationService;
    }

    public ComServer parse (JSONObject queryResult) throws JSONException {
        Object comServerJSon = queryResult.get(RemoteComServerQueryJSonPropertyNames.SINGLE_OBJECT_RESULT);
        return engineConfigurationService.parseComServerQueryResult((JSONObject) comServerJSon);
    }
}