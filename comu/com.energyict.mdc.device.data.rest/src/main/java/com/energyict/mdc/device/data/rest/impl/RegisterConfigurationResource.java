package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.configuration.rest.RegisterConfigInfo;
import com.energyict.mdc.device.configuration.rest.RegisterConfigurationComparator;
import com.energyict.mdc.device.data.Device;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

public class RegisterConfigurationResource {

    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public RegisterConfigurationResource(ResourceHelper resourceHelper, ExceptionFactory exceptionFactory) {
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getRegisters(@PathParam("mRID") String mRID, @BeanParam QueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
        List<RegisterSpec> registerSpecs = ListPager.of(deviceConfiguration.getRegisterSpecs(), new RegisterConfigurationComparator()).from(queryParameters).find();
        List<RegisterConfigInfo> registerSpecInfos = RegisterConfigInfo.from(registerSpecs);
        return PagedInfoList.asJson("data", registerSpecInfos, queryParameters);
    }

    @GET
    @Path("/{registerId}")
    @Produces(MediaType.APPLICATION_JSON)
    public RegisterConfigInfo getRegister(@PathParam("mRID") String mRID, @PathParam("registerId") long registerConfigId) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
        RegisterSpec registerSpec = findRegisterSpecOrThrowException(deviceConfiguration, registerConfigId);
        return RegisterConfigInfo.from(registerSpec);
    }

    private RegisterSpec findRegisterSpecOrThrowException(DeviceConfiguration deviceConfiguration, long registerConfigId) {
        for (RegisterSpec registerSpec : deviceConfiguration.getRegisterSpecs()) {
            if (registerSpec.getId() == registerConfigId) {
                return registerSpec;
            }
        }
        throw exceptionFactory.newException(MessageSeeds.NO_SUCH_REGISTER, registerConfigId);
    }
}
