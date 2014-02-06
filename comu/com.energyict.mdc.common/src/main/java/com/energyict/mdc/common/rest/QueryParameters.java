package com.energyict.mdc.common.rest;

import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;

public class QueryParameters {

    private final UriInfo uriInfo;

    @Inject
    public QueryParameters(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    public Integer getStart() {
        return getIntegerOrNull("start");
    }

    public Integer getLimit() {
        return getIntegerOrNull("limit");
    }

    private Integer getIntegerOrNull(String name) {
        String start = uriInfo.getQueryParameters().getFirst(name);
        if (start!=null) {
            return Integer.parseInt(start);
        }
        return null;
    }

}
