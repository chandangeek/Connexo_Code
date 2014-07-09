package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Register;
import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

public class RegisterResource {

    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public RegisterResource(ResourceHelper resourceHelper, ExceptionFactory exceptionFactory) {
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getRegisters(@PathParam("mRID") String mRID, @BeanParam QueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        List<Register> registers = ListPager.of(device.getRegisters(), new RegisterComparator()).from(queryParameters).find();
        List<RegisterInfo> registerInfos = RegisterInfo.fromList(registers);
        return PagedInfoList.asJson("data", registerInfos, queryParameters);
    }

    @GET
    @Path("/{registerId}")
    @Produces(MediaType.APPLICATION_JSON)
    public RegisterInfo getRegister(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        Register register = findRegisterOrThrowException(device, registerId);
        return RegisterInfo.from(register);
    }

    private Register findRegisterOrThrowException(Device device, long registerId) {
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
