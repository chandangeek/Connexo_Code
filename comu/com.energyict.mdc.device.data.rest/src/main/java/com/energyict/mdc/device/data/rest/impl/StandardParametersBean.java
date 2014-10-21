package com.energyict.mdc.device.data.rest.impl;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class StandardParametersBean {

    private UriInfo uriInfo;
    private boolean wasRegExp;
    private boolean wasMultiValued;

    public StandardParametersBean(@Context UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    public List<String> get(String key) {
        return getQueryParameters().get(key);
    }

    /**
     * Checks that the last call to {@link #getFirst(String)}
     * returned a value that is actually a regular expression.
     *
     * @return A flag that indicates if the last value returned by getFirst was a regular expression
     */
    public boolean wasRegExp() {
        return wasRegExp;
    }

    /**
     * Checks that the last call to {@link #getFirst(String)}
     * returned a value that is actually a comman separated list of values.
     *
     * @return A flag that indicates if the last value returned by getFirst is a comma separated list of values
     */
    public boolean wasMultiValued() {
        return wasMultiValued;
    }

    public boolean containsKey(String key) {
        return getQueryParameters().containsKey(key);
    }

    public String getFirst(String key){
        wasRegExp = false;
        wasMultiValued = false;
        List<String> values = get(key);
        if (values != null && !values.isEmpty()) {
            String value = values.get(0);
            return processedValue(value);
        }
        return null;
    }

    private String processedValue(String value) {
        if (value.contains("*")) {
            value = value.replaceAll("\\*","%");
            wasRegExp = true;
        }
        if (value.contains("?")) {
            value = value.replaceAll("\\?","_");
            wasRegExp = true;
        }
        if (value.contains("%")) {
            wasRegExp = true;
        }
        if (value.contains(",")) {
            wasMultiValued = true;
        }
        return value;
    }

    public List<Long> getLong(String key) {
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

}