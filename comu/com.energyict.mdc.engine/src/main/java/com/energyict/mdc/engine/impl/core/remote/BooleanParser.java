/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.remote;

import com.energyict.mdc.engine.impl.core.RemoteComServerQueryJSonPropertyNames;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Parses a JSONObject that was returned by a remote query to a Boolean.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-11 (09:52)
 */
public class BooleanParser {

    public boolean parse (JSONObject queryResult) throws JSONException {
        Object booleanJSon = queryResult.get(RemoteComServerQueryJSonPropertyNames.SINGLE_OBJECT_RESULT);
        return Boolean.parseBoolean((String) booleanJSon);
    }

}