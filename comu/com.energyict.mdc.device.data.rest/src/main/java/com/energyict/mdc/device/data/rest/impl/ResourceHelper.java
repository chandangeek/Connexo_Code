package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.Register;
import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.List;

public class ResourceHelper {

    private final DeviceDataService deviceDataService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public ResourceHelper(DeviceDataService deviceDataService, ExceptionFactory exceptionFactory) {
        super();
        this.deviceDataService = deviceDataService;
        this.exceptionFactory = exceptionFactory;
    }

    public Device findDeviceByIdOrThrowException(long id) {
        Device device = deviceDataService.findDeviceById(id);
        if (device == null) {
            throw new WebApplicationException("No device with id " + id, Response.Status.NOT_FOUND);
        }
        return device;
    }

    public Device findDeviceByMrIdOrThrowException(String mRID) {
        Device device = deviceDataService.findByUniqueMrid(mRID);
        if (device == null) {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_DEVICE, mRID);
        }
        return device;
    }

    public Register findRegisterOrThrowException(Device device, long registerId) {
        List<Register> registers = device.getRegisters();
        for(Register register : registers) {
            Optional<RegisterSpec> registerSpecOptional = Optional.fromNullable(register.getRegisterSpec());
            if(registerSpecOptional.isPresent() && registerSpecOptional.get().getId() == registerId) {
                return register;
            }
        }

        throw exceptionFactory.newException(MessageSeeds.NO_SUCH_REGISTER, registerId);
    }

}
