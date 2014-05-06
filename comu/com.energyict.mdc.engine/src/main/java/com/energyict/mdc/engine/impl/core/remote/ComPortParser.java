package com.energyict.mdc.engine.impl.core.remote;

import com.energyict.mdc.engine.impl.core.RemoteComServerQueryJSonPropertyNames;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.EngineModelService;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Parses a JSONObject that was returned by a remote query to a {@link ComPort}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-11 (09:52)
 */
public class ComPortParser {

    private final EngineModelService engineModelService;

    public ComPortParser(EngineModelService engineModelService) {
        this.engineModelService = engineModelService;
    }

    public ComPort parse (JSONObject queryResult) throws JSONException {
        Object comPortJSon = queryResult.get(RemoteComServerQueryJSonPropertyNames.SINGLE_OBJECT_RESULT);
        return engineModelService.parseComPortQueryResult((JSONObject) comPortJSon);
    }
}