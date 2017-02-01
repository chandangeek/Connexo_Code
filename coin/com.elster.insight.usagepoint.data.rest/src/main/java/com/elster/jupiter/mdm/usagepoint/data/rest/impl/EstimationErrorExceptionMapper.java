/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class EstimationErrorExceptionMapper implements ExceptionMapper<EstimationErrorException> {

    @Override
    public Response toResponse(EstimationErrorException exception) {
        return Response.status(Response.Status.BAD_REQUEST).entity(EstimationErrorInfo.from(exception)).build();
    }
}
