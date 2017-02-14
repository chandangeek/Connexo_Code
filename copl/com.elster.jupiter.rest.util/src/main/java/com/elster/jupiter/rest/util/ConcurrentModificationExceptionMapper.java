/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class ConcurrentModificationExceptionMapper implements ExceptionMapper<ConcurrentModificationException> {

    private final Provider<ConcurrentModificationInfo> infoProvider;
    private final HttpServletRequest request;

    @Inject
    public ConcurrentModificationExceptionMapper(Provider<ConcurrentModificationInfo> infoProvider, @Context HttpServletRequest request) {
        this.infoProvider = infoProvider;
        this.request = request;
    }

    @Override
    public Response toResponse(ConcurrentModificationException exception) {
        String httpMethod = request != null ? request.getMethod() : HttpMethod.PUT;
        ConcurrentModificationException contextDependentException = exception.withContext(ConcurrentModificationException.ExceptionContext.from(httpMethod));
        return Response.status(Response.Status.CONFLICT).entity(infoProvider.get().from(contextDependentException)).build();
    }
}
