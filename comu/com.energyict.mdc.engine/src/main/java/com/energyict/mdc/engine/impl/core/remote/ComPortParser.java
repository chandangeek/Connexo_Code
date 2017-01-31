/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.remote;

import com.energyict.mdc.engine.config.ComPort;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Parses a JSONObject that was returned by a remote query to a {@link ComPort}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-11 (09:52)
 */
class ComPortParser {

    public ComPort parse (JSONObject queryResult) throws JSONException {
        throw new UnsupportedOperationException("Remote com server is not fully supported yet in Connexo");
    }

}