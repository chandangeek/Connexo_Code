package com.energyict.mdc.multisense.api.impl.utils;

import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolationException;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;


public class DeviceLifeCycleActionViolationExceptionMapper implements ExceptionMapper<DeviceLifeCycleActionViolationException>{

    private final Provider<ConstraintViolationInfo> infoProvider;

    @Inject
    public DeviceLifeCycleActionViolationExceptionMapper(Provider<ConstraintViolationInfo> infoProvider) {
        this.infoProvider = infoProvider;
    }

    @Override
    public Response toResponse(DeviceLifeCycleActionViolationException exception) {
        ConstraintViolationInfo constraintViolationInfo = infoProvider.get();
        constraintViolationInfo.message= exception.getLocalizedMessage();
        constraintViolationInfo.error= exception.getMessage();

        return Response.status(Response.Status.BAD_REQUEST).entity(constraintViolationInfo).build();
    }

}
