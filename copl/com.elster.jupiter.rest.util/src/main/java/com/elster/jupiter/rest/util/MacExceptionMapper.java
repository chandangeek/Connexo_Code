/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util;

import com.elster.jupiter.orm.MacException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MacExceptionMapper implements ExceptionMapper<MacException> {

    private static final Logger LOGGER = Logger.getLogger(GenericExceptionMapper.class.getName());
    private final Provider<MacExceptionInfo> infoProvider;

    @Inject
    public MacExceptionMapper(Provider<MacExceptionInfo> infoProvider, @Context HttpServletRequest request) {
        this.infoProvider = infoProvider;
    }

    @Override
    public Response toResponse(MacException exception) {
        MacExceptionInfo exceptionInfo = infoProvider.get();
        exceptionInfo = exceptionInfo.from(exception);
        log(exceptionInfo.errorCode, exception);
        return Response.status(550).entity(exceptionInfo).build();
    }

    private void log(String errorReference, Exception exception) {
        LOGGER.log(Level.SEVERE, errorReference + " - " + exception.getMessage(), exception);
    }

}
