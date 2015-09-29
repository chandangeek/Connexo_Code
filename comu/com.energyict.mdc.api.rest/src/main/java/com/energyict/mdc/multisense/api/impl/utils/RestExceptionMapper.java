package com.energyict.mdc.multisense.api.impl.utils;

import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolationException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;


public class RestExceptionMapper implements ExceptionMapper<WebApplicationException>{

    private final Provider<ConstraintViolationInfo> infoProvider;

    @Inject
    public RestExceptionMapper(Provider<ConstraintViolationInfo> infoProvider) {
        this.infoProvider = infoProvider;
    }

    @Override
    public Response toResponse(WebApplicationException exception) {
        ConstraintViolationInfo constraintViolationInfo = infoProvider.get();
        constraintViolationInfo.message= exception.getLocalizedMessage();
        constraintViolationInfo.error= exception.getMessage();

        return Response.status(Response.Status.BAD_REQUEST).entity(constraintViolationInfo).build();
    }

}
