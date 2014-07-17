package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.configuration.rest.RegisterConfigInfo;
import com.energyict.mdc.device.configuration.rest.RegisterConfigurationComparator;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.MeasurementType;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.protocol.api.device.MultiplierMode;
import com.google.common.base.Optional;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class RegisterConfigurationResource {

    private final ResourceHelper resourceHelper;
    private final MasterDataService masterDataService;

    @Inject
    public RegisterConfigurationResource(ResourceHelper resourceHelper, MasterDataService masterDataService) {
        super();
        this.resourceHelper = resourceHelper;
        this.masterDataService = masterDataService;
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
    public Response createRegisterConfig(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, RegisterConfigInfo registerConfigInfo) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        RegisterType registerType = registerConfigInfo.registerType ==null?null: findRegisterTypeOrThrowException(registerConfigInfo.registerType);
        RegisterSpec registerSpec = deviceConfiguration.createRegisterSpec(registerType)
                .setMultiplierMode(MultiplierMode.CONFIGURED_ON_OBJECT)
                .setMultiplier(registerConfigInfo.multiplier)
                .setNumberOfDigits(registerConfigInfo.numberOfDigits)
                .setNumberOfFractionDigits(registerConfigInfo.numberOfFractionDigits)
                .setOverflow(registerConfigInfo.overflow)
                .setOverruledObisCode(registerConfigInfo.overruledObisCode)
                .add();
        return Response.status(Response.Status.CREATED).entity(RegisterConfigInfo.from(registerSpec)).build();
    }

    @PUT
    @Path("/{registerConfigId}")
    @Produces(MediaType.APPLICATION_JSON)
    public RegisterConfigInfo updateRegisterConfig(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, @PathParam("registerConfigId") long registerTypeId, RegisterConfigInfo registerConfigInfo) {
        RegisterSpec registerSpec = findRegisterSpecOrThrowException(deviceTypeId, deviceConfigurationId, registerTypeId);
        RegisterType registerType = registerConfigInfo.registerType ==null?null:resourceHelper.findRegisterTypeByIdOrThrowException(registerConfigInfo.registerType);
        registerConfigInfo.writeTo(registerSpec, registerType);
        registerSpec.save();
        return RegisterConfigInfo.from(findRegisterSpecOrThrowException(deviceTypeId, deviceConfigurationId, registerTypeId));
    }

    @DELETE
    @Path("/{registerConfigId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteRegisterConfig(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, @PathParam("registerConfigId") long registerTypeId) {
        RegisterSpec registerSpec = findRegisterSpecOrThrowException(deviceTypeId, deviceConfigurationId, registerTypeId);
        registerSpec.getDeviceConfiguration().deleteRegisterSpec(registerSpec);
        return Response.ok().build();
    }

    private RegisterType findRegisterTypeOrThrowException(Long registerTypeId) {
        Optional<RegisterType> registerType = masterDataService.findRegisterType(registerTypeId);
        if (!registerType.isPresent()) {
            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_REFERENCE_TO_REGISTER_TYPE, "registerType");
        }
        return registerType.get();
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
