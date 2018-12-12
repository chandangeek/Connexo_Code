/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.monitor.app.impl.rest.resource;

import org.json.JSONException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class JSonException extends WebApplicationException {

    JSonException(JSONException ex){
        super(Response.serverError().entity(ex.getMessage()).type("text/plain").build());
    }

}
