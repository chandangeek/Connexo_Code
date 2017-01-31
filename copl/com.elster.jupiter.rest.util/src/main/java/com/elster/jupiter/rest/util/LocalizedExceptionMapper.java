/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util;

import com.elster.jupiter.nls.LocalizedException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;


public class LocalizedExceptionMapper implements ExceptionMapper<LocalizedException>{

    private final Provider<ConstraintViolationInfo> infoProvider;

    @Inject
    public LocalizedExceptionMapper(Provider<ConstraintViolationInfo> infoProvider) {
        this.infoProvider = infoProvider;
    }

    @Override
    public Response toResponse(LocalizedException exception) {
        ConstraintViolationInfo constraintViolationInfo = infoProvider.get();
        constraintViolationInfo.from(exception);

        return Response.status(StatusCode.UNPROCESSABLE_ENTITY.getStatusCode()).entity(constraintViolationInfo).build();
    }

}
