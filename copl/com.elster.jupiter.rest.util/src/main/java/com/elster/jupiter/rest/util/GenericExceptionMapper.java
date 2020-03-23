/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GenericExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOGGER = Logger.getLogger(GenericExceptionMapper.class.getName());
    private final Provider<GenericExceptionInfo> infoProvider;

    @Inject
    public GenericExceptionMapper(Provider<GenericExceptionInfo> infoProvider) {
        this.infoProvider = infoProvider;
    }

    @Override
    public Response toResponse(Exception exception) {
        GenericExceptionInfo exceptionInfo = infoProvider.get();
        exceptionInfo = exceptionInfo.from(exception);
        log(exceptionInfo.errorCode, exception);
        return Response.status(getStatus(exception)).entity(exceptionInfo).build();
    }

    private Response.Status getStatus(Exception exception) {
        if (exception instanceof WebApplicationException) {
            WebApplicationException webEx = (WebApplicationException) exception;
            return Response.Status.fromStatusCode(webEx.getResponse().getStatus());
        } else {
            return Response.Status.INTERNAL_SERVER_ERROR;

        }
    }
    private void log(String errorReference, Exception exception) {
        LOGGER.log(Level.SEVERE, errorReference + " - " + exception.getMessage(), exception);
    }
}

