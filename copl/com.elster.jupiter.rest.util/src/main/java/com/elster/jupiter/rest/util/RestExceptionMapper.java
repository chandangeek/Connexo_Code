/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;


public class RestExceptionMapper implements ExceptionMapper<ExceptionFactory.RestException> {

    private final Provider<ConstraintViolationInfo> infoProvider;

    @Inject
    public RestExceptionMapper(Provider<ConstraintViolationInfo> infoProvider) {
        this.infoProvider = infoProvider;
    }

    @Override
    public Response toResponse(ExceptionFactory.RestException exception) {
        ConstraintViolationInfo constraintViolationInfo = infoProvider.get();
        constraintViolationInfo.from(exception);
        return Response.status(exception.getStatus()).entity(constraintViolationInfo).build();
    }

}
