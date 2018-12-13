/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.redknee;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Created by bvn on 9/25/15.
 */
public class SimExceptionMapper implements ExceptionMapper<WebApplicationException> {
    @Override
    public Response toResponse(WebApplicationException e) {
        ErrorInfo errorInfo = new ErrorInfo();
        errorInfo.message = e.getMessage();
        return Response.status(e.getResponse().getStatus()).entity(errorInfo).build();
    }

    class ErrorInfo {
        public String message;
    }
}
