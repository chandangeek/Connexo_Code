/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;


public class LocalizedFieldValidationExceptionMapper implements ExceptionMapper<LocalizedFieldValidationException>{


    private final Provider<ConstraintViolationInfo> infoProvider;

    @Inject
    public LocalizedFieldValidationExceptionMapper(Provider<ConstraintViolationInfo> infoProvider) {
        this.infoProvider = infoProvider;
    }

    @Override
    public Response toResponse(LocalizedFieldValidationException fieldException) {
        return Response.status(Response.Status.BAD_REQUEST).entity(infoProvider.get().from(fieldException)).build();
    }

}
