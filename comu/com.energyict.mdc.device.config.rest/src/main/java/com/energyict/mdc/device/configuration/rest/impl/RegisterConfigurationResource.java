package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.RegisterSpec;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class RegisterConfigurationResource {

    private final ResourceHelper resourceHelper;

    @Inject
    public RegisterConfigurationResource(ResourceHelper resourceHelper) {
        this.resourceHelper = resourceHelper;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getRegisterConfigs(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, @BeanParam QueryParameters queryParameters) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        List<RegisterSpec> pagedRegisterSpecs = ListPager.of(deviceConfiguration.getRegisterSpecs(), new RegisterConfigurationComparator()).from(queryParameters).find();
        List<RegisterConfigInfo> registerConfigInfos = RegisterConfigInfo.from(pagedRegisterSpecs);
        return PagedInfoList.asJson("registerConfigurations", registerConfigInfos, queryParameters);
    }

    @GET
    @Path("/{registerId}")
    @Produces(MediaType.APPLICATION_JSON)
    public RegisterConfigInfo getRegisterConfigs(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, @PathParam("registerId") long registerId) {
        return RegisterConfigInfo.from(findRegisterSpecOrThrowException(deviceTypeId,deviceConfigurationId, registerId));
    }

    private RegisterSpec findRegisterSpecOrThrowException(long deviceTypeId, long deviceConfigId, long registerId) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigId);
        for (RegisterSpec registerSpec : deviceConfiguration.getRegisterSpecs()) {
            if (registerSpec.getId()==registerId) {
                return registerSpec;
            }
        }
        throw new WebApplicationException("No such register configuration for the device configuration", Response.status(Response.Status.NOT_FOUND).entity("No such register configuration for the device configuration").build());
    }


}
