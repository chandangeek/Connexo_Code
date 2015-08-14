package com.elster.jupiter.rest.util;

import com.elster.jupiter.orm.OptimisticLockException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class OptimisticLockExceptionMapper implements ExceptionMapper<OptimisticLockException>{


    private final Provider<ConstraintViolationInfo> infoProvider;

    @Inject
    public OptimisticLockExceptionMapper(Provider<ConstraintViolationInfo> infoProvider) {
        this.infoProvider = infoProvider;
    }

    @Override
    public Response toResponse(OptimisticLockException exception) {
        return Response.status(Response.Status.BAD_REQUEST).entity(infoProvider.get().from(exception)).build();
    }

}
