/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GenericExceptionMapper<T extends Throwable> implements ExceptionMapper<T> {

    private static final Logger LOGGER = Logger.getLogger(GenericExceptionMapper.class.getName());
    private final Provider<GenericExceptionInfo> infoProvider;

    @Inject
    public GenericExceptionMapper(Provider<GenericExceptionInfo> infoProvider) {
        this.infoProvider = infoProvider;
    }

    @Override
    public Response toResponse(Throwable exception) {
        GenericExceptionInfo exceptionInfo = infoProvider.get();
        //TODO - add translation to message and from() method to exceptionInfo
        exceptionInfo.message = "Internal server error dummy";
        exceptionInfo.error = exception.getLocalizedMessage();
        exceptionInfo.errorCode = getErrorCode();
        log(getErrorCode(), exception);
        return Response.status(getStatus(exception)).entity(exceptionInfo).build();
    }

    private Response.Status getStatus(Throwable exception) {
        if (exception instanceof WebApplicationException) {
            WebApplicationException webEx = (WebApplicationException) exception;
            return Response.Status.fromStatusCode(webEx.getResponse().getStatus());
        } else {
            return Response.Status.INTERNAL_SERVER_ERROR;

        }
    }


    private void log(String errorReference, Throwable exception) {
        LOGGER.log(Level.SEVERE, errorReference + " - " + exception.getMessage(), exception);
    }

    private String getErrorCode() {
        return getHostname() + "-" + Long.toHexString(System.currentTimeMillis()).toUpperCase();
    }

    private String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "UNKOWNHOST";
        }
    }
}

