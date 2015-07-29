package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.TextualRegisterSpec;
import com.energyict.mdc.device.config.security.Privileges;
import com.energyict.mdc.device.configuration.rest.RegisterConfigInfo;
import com.energyict.mdc.device.configuration.rest.RegisterConfigurationComparator;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterType;

import javax.annotation.security.RolesAllowed;
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
import java.util.List;
import java.util.Optional;

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
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_TYPE, Privileges.VIEW_DEVICE_TYPE})
    public PagedInfoList getRegisterConfigs(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, @BeanParam JsonQueryParameters queryParameters) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        List<RegisterSpec> pagedRegisterSpecs = ListPager.of(deviceConfiguration.getRegisterSpecs(), new RegisterConfigurationComparator()).from(queryParameters).find();
        List<RegisterConfigInfo> registerConfigInfos = RegisterConfigInfo.from(pagedRegisterSpecs);
        return PagedInfoList.fromPagedList("registerConfigurations", registerConfigInfos, queryParameters);
    }

    @GET
    @Path("/{registerId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_TYPE, Privileges.VIEW_DEVICE_TYPE})
    public RegisterConfigInfo getRegisterConfigs(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, @PathParam("registerId") long registerId) {
        return RegisterConfigInfo.from(findRegisterSpecOrThrowException(deviceTypeId, deviceConfigurationId, registerId));
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_TYPE)
    public Response createRegisterConfig(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, RegisterConfigInfo registerConfigInfo) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        RegisterType registerType = registerConfigInfo.registerType ==null?null: findRegisterTypeOrThrowException(registerConfigInfo.registerType);
        RegisterSpec registerSpec = createRegisterSpec(registerConfigInfo, deviceConfiguration, registerType);
        return Response.status(Response.Status.CREATED).entity(RegisterConfigInfo.from(registerSpec)).build();
    }

    private RegisterSpec createRegisterSpec(RegisterConfigInfo registerConfigInfo, DeviceConfiguration deviceConfiguration, RegisterType registerType) {
        RegisterSpec registerSpec;
        if(registerConfigInfo.asText){
            registerSpec = deviceConfiguration.createTextualRegisterSpec(registerType)
                    .setOverruledObisCode(registerConfigInfo.overruledObisCode)
                    .add();
        } else {
            registerSpec = deviceConfiguration.createNumericalRegisterSpec(registerType)
                    .setNumberOfDigits(registerConfigInfo.numberOfDigits)
                    .setNumberOfFractionDigits(registerConfigInfo.numberOfFractionDigits)
                    .setOverflowValue(registerConfigInfo.overflow)
                    .setOverruledObisCode(registerConfigInfo.overruledObisCode)
                    .add();
        }
        return registerSpec;
    }

    @PUT
    @Path("/{registerConfigId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_TYPE)
    public RegisterConfigInfo updateRegisterConfig(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, @PathParam("registerConfigId") long registerConfigId, RegisterConfigInfo registerConfigInfo) {
        RegisterSpec registerSpec = findRegisterSpecOrThrowException(deviceTypeId, deviceConfigurationId, registerConfigId);
        RegisterType registerType = registerConfigInfo.registerType ==null?null:resourceHelper.findRegisterTypeByIdOrThrowException(registerConfigInfo.registerType);
        if(registerConfigInfo.asText == registerSpec.isTextual()){
            registerConfigInfo.writeTo(registerSpec, registerType);
            if (registerConfigInfo.asText) {
                registerSpec.getDeviceConfiguration().getRegisterSpecUpdaterFor((TextualRegisterSpec) registerSpec).update();
            } else {
                registerSpec.getDeviceConfiguration().getRegisterSpecUpdaterFor((NumericalRegisterSpec) registerSpec).update();
            }
        } else {
            registerSpec.getDeviceConfiguration().deleteRegisterSpec(registerSpec);
            RegisterSpec newRegisterSpec = createRegisterSpec(registerConfigInfo, registerSpec.getDeviceConfiguration(), registerType);
            registerConfigId = newRegisterSpec.getId();
        }
        return RegisterConfigInfo.from(findRegisterSpecOrThrowException(deviceTypeId, deviceConfigurationId, registerConfigId));
    }

    @DELETE
    @Path("/{registerConfigId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_TYPE)
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
