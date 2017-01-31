/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.api.util.v1.hypermedia;

import com.elster.jupiter.rest.util.ExceptionFactory;

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
        constraintViolationInfo.message = exception.getLocalizedMessage();
        constraintViolationInfo.error = exception.getMessageSeed().getKey();

        return Response.status(exception.getStatus()).entity(constraintViolationInfo).build();
    }

//    private String getErrorId(MessageSeed messageSeed) {
//        return PublicRestApplication.COMPONENT_NAME + new DecimalFormat("0000").format(messageSeed.getNumber()) + messageSeed.getLevel().getName().substring(0,1).toUpperCase();
//    }
}
