package com.energyict.mdc.engine.monitor.impl.rest.resource;

import org.json.JSONException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class JSonException extends WebApplicationException {

    JSonException(JSONException ex){
        super(Response.serverError().entity(ex.getMessage()).type("text/plain").build());
    }

}
