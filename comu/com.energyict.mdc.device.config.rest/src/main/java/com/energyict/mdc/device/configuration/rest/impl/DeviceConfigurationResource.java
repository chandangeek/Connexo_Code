package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
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

public class DeviceConfigurationResource {

    private final ResourceHelper resourceHelper;
    private final DeviceConfigurationService deviceConfigurationService;
    private final Provider<RegisterConfigurationResource> registerConfigurationResourceProvider;

    @Inject
    public DeviceConfigurationResource(ResourceHelper resourceHelper, DeviceConfigurationService deviceConfigurationService, Provider<RegisterConfigurationResource> registerConfigurationResourceProvider) {
        this.resourceHelper = resourceHelper;
        this.deviceConfigurationService = deviceConfigurationService;
        this.registerConfigurationResourceProvider = registerConfigurationResourceProvider;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getDeviceConfigurationsForDeviceType(@PathParam("deviceTypeId") long id, @BeanParam QueryParameters queryParameters) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        List<DeviceConfiguration> deviceConfigurations =
                deviceConfigurationService.
                        findDeviceConfigurationsUsingDeviceType(deviceType).
                        from(queryParameters).
                        find();
        return PagedInfoList.asJson("deviceConfigurations", DeviceConfigurationInfo.from(deviceConfigurations), queryParameters);
    }

    @GET
    @Path("/{deviceConfigurationId}")
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceConfigurationInfo getDeviceConfigurationsById(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        for (DeviceConfiguration deviceConfiguration : deviceType.getConfigurations()) {
            if (deviceConfiguration.getId()==deviceConfigurationId) {
                return new DeviceConfigurationInfo(deviceConfiguration);
            }
        }
        throw new WebApplicationException("No such device configuration for the device type", Response.status(Response.Status.NOT_FOUND).entity("No such device configuration for the device type").build());
    }

    @DELETE
    @Path("/{deviceConfigurationId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteDeviceConfigurations(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        deviceType.removeConfiguration(deviceConfiguration);
        return Response.ok().build();
    }

    @PUT
    @Path("/{deviceConfigurationId}")
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceConfigurationInfo updateDeviceConfigurations(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, DeviceConfigurationInfo deviceConfigurationInfo) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        if (deviceConfigurationInfo.active!=null && deviceConfigurationInfo.active && !deviceConfiguration.isActive()) {
            deviceConfiguration.activate();
        } else if (deviceConfigurationInfo.active!=null && !deviceConfigurationInfo.active && deviceConfiguration.isActive()) {
            deviceConfiguration.deactivate();
        } else {
            deviceConfigurationInfo.writeTo(deviceConfiguration);
        }
        deviceConfiguration.save();
        return new DeviceConfigurationInfo(deviceConfiguration);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceConfigurationInfo createDeviceConfiguration(@PathParam("deviceTypeId") long deviceTypeId, DeviceConfigurationInfo deviceConfigurationInfo) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration(deviceConfigurationInfo.name).
                description(deviceConfigurationInfo.description);
        if (deviceConfigurationInfo.canBeGateway!=null) {
            deviceConfigurationBuilder.canActAsGateway(deviceConfigurationInfo.canBeGateway);
        }
        if (deviceConfigurationInfo.isDirectlyAddressable!=null) {
            deviceConfigurationBuilder.isDirectlyAddressable(deviceConfigurationInfo.isDirectlyAddressable);
        }
        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();
        return new DeviceConfigurationInfo(deviceConfiguration);
    }

    @Path("/{deviceConfigurationId}/registerconfigurations")
    public RegisterConfigurationResource getRegisterConfigResource() {
        return registerConfigurationResourceProvider.get();
    }

}
