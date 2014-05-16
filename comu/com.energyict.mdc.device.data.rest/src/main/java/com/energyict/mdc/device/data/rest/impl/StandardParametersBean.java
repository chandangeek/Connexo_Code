package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.util.conditions.Order;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class StandardParametersBean {

    private UriInfo uriInfo;
    private boolean isRegExp;

    public StandardParametersBean(@Context UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    public List<String> get(Object key) {
        return getQueryParameters().get(key);
    }

    public String getFirst(Object key){
        isRegExp = false;
        List<String> values = get(key);
        if (values != null && values.size() > 0) {
            String value = values.get(0);
            if (value.contains("*")) {
                value = value.replaceAll("\\*","%");
                isRegExp = true;
            }
            return value;
        }
        return null;
    }

    public List<Long> getLong(Object key) {
        List<String> original = get(key);
        List<Long> resultList = new ArrayList<>();
        if (original != null) {
            for (String param : original){
                try {
                    resultList.add(Long.parseLong(param));
                } catch (NumberFormatException ex) {
                    throw new WebApplicationException(Response.Status.BAD_REQUEST);
                }
            }
        }
        return resultList;
    }



    public MultivaluedMap<String, String> getQueryParameters(){
        return getUriInfo().getQueryParameters();
    }

    public UriInfo getUriInfo() {
        return uriInfo;
    }

    public boolean isRegExp() {
        return isRegExp;
    }
}