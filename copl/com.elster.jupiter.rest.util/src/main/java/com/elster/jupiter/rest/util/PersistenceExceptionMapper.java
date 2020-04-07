/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util;

import com.elster.jupiter.util.exception.PersistenceException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PersistenceExceptionMapper implements ExceptionMapper<PersistenceException> {

    private static final Logger LOGGER = Logger.getLogger(PersistenceExceptionMapper.class.getName());
    private final Provider<SensitiveExceptionInfo> infoProvider;

    @Inject
    public PersistenceExceptionMapper(Provider<SensitiveExceptionInfo> infoProvider) {
        this.infoProvider = infoProvider;
    }

    @Override
    public Response toResponse(PersistenceException exception) {
        SensitiveExceptionInfo exceptionInfo = infoProvider.get();
        exceptionInfo = exceptionInfo.from(exception);
        LOGGER.log(Level.SEVERE, exceptionInfo.errorCode + " - " + exception.getMessage(), exception);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(exceptionInfo).build();
    }

}

