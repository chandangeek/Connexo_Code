package com.energyict.mdc.multisense.api.impl.utils;

import com.elster.jupiter.rest.util.ExceptionFactory;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.ws.rs.core.Response;
import java.util.Set;

/**
 * Created by bvn on 7/15/15.
 */
public class ResourceHelper {

    private final DeviceService deviceService;
    private final ExceptionFactory exceptionFactory;
    private final Validator validator;

    @Inject
    public ResourceHelper(DeviceService deviceService, ExceptionFactory exceptionFactory, Validator validator) {
        this.deviceService = deviceService;
        this.exceptionFactory = exceptionFactory;
        this.validator = validator;
    }

    public Device findDeviceByMrIdOrThrowException(String mrid) {
        return deviceService
                .findDeviceByMrid(mrid)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE));
    }

    public void validate(Object info, Class<?> clazz) {
        Set<ConstraintViolation<Object>> violations = validator.validate(info, clazz);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

    }
}
