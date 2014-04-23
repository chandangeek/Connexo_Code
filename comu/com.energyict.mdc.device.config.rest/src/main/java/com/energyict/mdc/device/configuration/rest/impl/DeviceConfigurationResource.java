package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

public class DeviceConfigurationResource {

    private final ResourceHelper resourceHelper;
    private final DeviceConfigurationService deviceConfigurationService;
    private final Provider<RegisterConfigurationResource> registerConfigurationResourceProvider;
    private final Provider<ConnectionMethodResource> connectionMethodResourceProvider;
    private final Provider<ProtocolDialectResource> protocolDialectResourceProvider;

    @Inject
    public DeviceConfigurationResource(ResourceHelper resourceHelper, DeviceConfigurationService deviceConfigurationService, Provider<RegisterConfigurationResource> registerConfigurationResourceProvider, Provider<ConnectionMethodResource> connectionMethodResourceProvider, Provider<ProtocolDialectResource> protocolDialectResourceProvider) {
        this.resourceHelper = resourceHelper;
        this.deviceConfigurationService = deviceConfigurationService;
        this.registerConfigurationResourceProvider = registerConfigurationResourceProvider;
        this.connectionMethodResourceProvider = connectionMethodResourceProvider;
        this.protocolDialectResourceProvider = protocolDialectResourceProvider;
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

    @Path("/{deviceConfigurationId}/connectionmethods")
    public ConnectionMethodResource getConnectionMethodResource() {
        return connectionMethodResourceProvider.get();
    }

    @Path("/{deviceConfigurationId}/protocoldialects")
       public ProtocolDialectResource getProtocolDialectsResource() {
           return protocolDialectResourceProvider.get();
       }

}
