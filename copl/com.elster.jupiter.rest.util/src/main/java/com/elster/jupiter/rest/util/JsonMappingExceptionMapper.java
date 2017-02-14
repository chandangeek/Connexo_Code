/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * When an JavaXmlBasedAdapter fails to convert JSON into a valid object, an exception thrown. This mapper will convert
 * that exception into a HTTP 400 and provide a JSON description of the issue in a format understood out of the box
 * by ExtJS
 */
public  class JsonMappingExceptionMapper implements ExceptionMapper<JsonMappingException> {


    private final Provider<ConstraintViolationInfo> infoProvider;

    @Inject
    public JsonMappingExceptionMapper(Provider<ConstraintViolationInfo> infoProvider) {
        this.infoProvider = infoProvider;
    }

    @Override
    public Response toResponse(JsonMappingException exception) {
        return Response.status(Response.Status.BAD_REQUEST).entity(infoProvider.get().from(exception)).build();
    }
}