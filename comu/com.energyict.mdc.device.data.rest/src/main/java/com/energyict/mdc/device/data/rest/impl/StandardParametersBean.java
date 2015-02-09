package com.energyict.mdc.device.data.rest.impl;

import java.util.List;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

public class StandardParametersBean {

    private UriInfo uriInfo;

    public StandardParametersBean(@Context UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    public String getFirst(String key){
        List<String> values = getQueryParameters().get(key);
        if (values != null && !values.isEmpty()) {
            return values.get(0);
        }
        return null;
    }

    public MultivaluedMap<String, String> getQueryParameters(){
        return uriInfo.getQueryParameters();
    }

}