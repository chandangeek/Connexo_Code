/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util;

import com.elster.jupiter.util.exception.GenericException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GenericExceptionMapper implements ExceptionMapper<GenericException> {

    private static final Logger LOGGER = Logger.getLogger(GenericExceptionMapper.class.getName());
    private final Provider<ConstraintViolationInfo> infoProvider;

    @Inject
    public GenericExceptionMapper(Provider<ConstraintViolationInfo> infoProvider) {
        this.infoProvider = infoProvider;
    }

    @Override
    public Response toResponse(GenericException exception) {
        ConstraintViolationInfo constraintViolationInfo = infoProvider.get();
        constraintViolationInfo.message = exception.getMessage();
        constraintViolationInfo.error = exception.getLocalizedMessage();
        constraintViolationInfo.errorCode = exception.getErrorCode();
        log(exception.getErrorCode(), exception);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(constraintViolationInfo).build();
    }


    private void log(String errorReference, Exception exception) {
        LOGGER.log(Level.SEVERE, errorReference + " - " + exception.getMessage(), exception);
    }
}

