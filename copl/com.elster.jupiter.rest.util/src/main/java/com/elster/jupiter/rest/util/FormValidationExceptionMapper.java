/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util;

import com.elster.jupiter.domain.util.FormValidationException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class FormValidationExceptionMapper implements ExceptionMapper<FormValidationException> {

    private final Provider<ConstraintViolationInfo> infoProvider;

    @Inject
    public FormValidationExceptionMapper(Provider<ConstraintViolationInfo> infoProvider) {
        this.infoProvider = infoProvider;
    }

    @Override
    public Response toResponse(FormValidationException exception) {
        return Response.status(Response.Status.BAD_REQUEST).entity(infoProvider.get().from(exception)).build();
    }
}