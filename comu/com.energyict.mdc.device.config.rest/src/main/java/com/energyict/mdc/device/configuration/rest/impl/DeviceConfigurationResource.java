package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.masterdata.LogBookType;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

public class DeviceConfigurationResource {

    private final ResourceHelper resourceHelper;
    private final DeviceConfigurationService deviceConfigurationService;
    private final Provider<RegisterConfigurationResource> registerConfigurationResourceProvider;
    private final Provider<ConnectionMethodResource> connectionMethodResourceProvider;
    private final Provider<ProtocolDialectResource> protocolDialectResourceProvider;
    private final Thesaurus thesaurus;

    @Inject
    public DeviceConfigurationResource(ResourceHelper resourceHelper, DeviceConfigurationService deviceConfigurationService, Provider<RegisterConfigurationResource> registerConfigurationResourceProvider, Provider<ConnectionMethodResource> connectionMethodResourceProvider, Provider<ProtocolDialectResource> protocolDialectResourceProvider, Thesaurus thesaurus) {
        this.resourceHelper = resourceHelper;
        this.deviceConfigurationService = deviceConfigurationService;
        this.registerConfigurationResourceProvider = registerConfigurationResourceProvider;
        this.connectionMethodResourceProvider = connectionMethodResourceProvider;
        this.protocolDialectResourceProvider = protocolDialectResourceProvider;
        this.thesaurus = thesaurus;
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

    @GET
    @Path("/{deviceConfigurationId}/logbookconfigurations")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDeviceConfigurationsLogBookConfigurations(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @BeanParam QueryParameters queryParameters,
            @QueryParam("available") String available) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        List<LogBookSpec> logBookSpecs = deviceConfiguration.getLogBookSpecs();
        List<LogBookTypeInfo> logBookTypes = new ArrayList<>(logBookSpecs.size());
        if (available != null && Boolean.parseBoolean(available)) {
            logBookTypes = LogBookTypeInfo.from(findAllAvailableLogBookTypesForDeviceConfiguration(deviceType, deviceConfiguration));
        } else {
            for (LogBookSpec logBookSpec : logBookSpecs) {
                logBookTypes.add(LogBookSpecInfo.from(logBookSpec));
            }
        }
        return Response.ok(PagedInfoList.asJson("data", logBookTypes, queryParameters)).build();
    }

    private List<LogBookType> findAllAvailableLogBookTypesForDeviceConfiguration(DeviceType deviceType, DeviceConfiguration deviceConfiguration) {
        List<LogBookType> allLogBookTypes = deviceType.getLogBookTypes();
        Iterator<LogBookType> logBookTypeIterator = allLogBookTypes.iterator();
        while (logBookTypeIterator.hasNext()) {
            LogBookType logBookType = logBookTypeIterator.next();
            if (deviceConfiguration.hasLogBookSpecForConfig((int) logBookType.getId(), 0)){
                logBookTypeIterator.remove();
            }
        }
        return allLogBookTypes;
    }

    @POST
    @Path("/{deviceConfigurationId}/logbookconfigurations")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createLogBooksSpecForDeviceConfiguartion(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            List<Long> ids) {
        if (ids == null || ids.size() == 0) {
            throw new TranslatableApplicationException(thesaurus, MessageSeeds.NO_LOGBOOK_TYPE_ID_FOR_ADDING);
        }
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        List<LogBookTypeInfo> addedLogBookSpecs = new ArrayList<>(ids.size());
        for (LogBookType logBookType : deviceType.getLogBookTypes()) {
            if(ids.contains(logBookType.getId())){
                LogBookSpec newLogBookSpec = deviceConfiguration.createLogBookSpec(logBookType).add();
                addedLogBookSpecs.add(LogBookSpecInfo.from(newLogBookSpec));
            }
        }
        return Response.ok(addedLogBookSpecs).build();
    }

    @DELETE
    @Path("/{deviceConfigurationId}/logbookconfigurations/{logBookSpecId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteLogBooksSpecFromDeviceConfiguartion(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @PathParam("logBookSpecId") long logBookSpecId) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        LogBookSpec logBookSpec = null;
        for (LogBookSpec spec : deviceConfiguration.getLogBookSpecs()) {
            if (spec.getId() == logBookSpecId){
                logBookSpec = spec;
                break;
            }
        }
        if (logBookSpec == null) {
            throw new TranslatableApplicationException(thesaurus, MessageSeeds.NO_LOGBOOK_TYPE_FOUND, logBookSpecId);
        }
        deviceConfiguration.deleteLogBookSpec(logBookSpec);
        return Response.ok().build();
    }

    @PUT
    @Path("/{deviceConfigurationId}/logbookconfigurations/{logBookSpecId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response editLogBookSpecForDeviceConfiguration(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @PathParam("logBookSpecId") long logBookSpecId,
            LogBookSpecInfo logBookRequest) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        List<LogBookSpec> logBookSpecs = new ArrayList<>(deviceConfiguration.getLogBookSpecs());
        for (LogBookSpec logBookSpec : logBookSpecs) {
            if (logBookSpec.getId() == logBookSpecId){
                deviceConfiguration.getLogBookSpecUpdaterFor(logBookSpec).setOverruledObisCode(logBookRequest.overruledObisCode).update();
                return Response.ok(LogBookSpecInfo.from(logBookSpec)).build();
            }
        }
        throw new TranslatableApplicationException(thesaurus, MessageSeeds.NO_LOGBOOK_TYPE_FOUND, logBookRequest.id);
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
