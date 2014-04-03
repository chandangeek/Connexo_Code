package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.*;
import com.energyict.mdc.protocol.api.device.MultiplierMode;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

public class RegisterConfigurationResource {

    private final DeviceConfigurationService deviceConfigurationService;
    private final ResourceHelper resourceHelper;

    @Inject
    public RegisterConfigurationResource(DeviceConfigurationService deviceConfigurationService, ResourceHelper resourceHelper) {
        this.deviceConfigurationService = deviceConfigurationService;
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
        return RegisterConfigInfo.from(findRegisterSpecOrThrowException(deviceTypeId, deviceConfigurationId, registerId));
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public RegisterConfigInfo createRegisterConfig(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, RegisterConfigInfo registerConfigInfo) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        RegisterMapping registerMapping = registerConfigInfo.registerMapping ==null?null:findRegisterMappingOrThrowException(registerConfigInfo.registerMapping);
        RegisterSpec registerSpec = deviceConfiguration.createRegisterSpec(registerMapping)
                .setMultiplierMode(MultiplierMode.CONFIGURED_ON_OBJECT)
                .setMultiplier(registerConfigInfo.multiplier)
                .setNumberOfDigits(registerConfigInfo.numberOfDigits)
                .setNumberOfFractionDigits(registerConfigInfo.numberOfFractionDigits)
                .setOverflow(registerConfigInfo.overflowValue)
                .setOverruledObisCode(registerConfigInfo.overruledObisCode)
                .add();
        return RegisterConfigInfo.from(registerSpec);
    }

    @PUT
    @Path("/{registerConfigId}")
    @Produces(MediaType.APPLICATION_JSON)
    public RegisterConfigInfo updateRegisterConfig(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, @PathParam("registerConfigId") long registerTypeId, RegisterConfigInfo registerConfigInfo) {
        RegisterSpec registerSpec = findRegisterSpecOrThrowException(deviceTypeId, deviceConfigurationId, registerTypeId);
        RegisterMapping registerMapping = registerConfigInfo.registerMapping ==null?null:findRegisterMappingOrThrowException(registerConfigInfo.registerMapping);
        registerConfigInfo.writeTo(registerSpec, registerMapping);
        registerSpec.save();
        return RegisterConfigInfo.from(registerSpec);
    }

    @DELETE
    @Path("/{registerConfigId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteRegisterConfig(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, @PathParam("registerConfigId") long registerTypeId) {
        RegisterSpec registerSpec = findRegisterSpecOrThrowException(deviceTypeId, deviceConfigurationId, registerTypeId);
        registerSpec.getDeviceConfiguration().deleteRegisterSpec(registerSpec);
        return Response.ok().build();
    }

    private RegisterMapping findRegisterMappingOrThrowException(long registerTypeId) {
        RegisterMapping registerMapping = deviceConfigurationService.findRegisterMapping(registerTypeId);
        if (registerMapping==null) {
            throw new WebApplicationException("No register type with id " + registerTypeId, Response.status(Response.Status.NOT_FOUND).entity("No register type with id " + registerTypeId).build());
        }
        return registerMapping;
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
