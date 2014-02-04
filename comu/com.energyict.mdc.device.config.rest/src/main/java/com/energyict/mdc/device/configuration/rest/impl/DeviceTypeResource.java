package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.common.services.SortOrder;
import com.energyict.mdc.services.DeviceConfigurationService;
import com.energyict.mdw.amr.RegisterMapping;
import com.energyict.mdw.core.DeviceConfiguration;
import com.energyict.mdw.core.DeviceType;
import com.energyict.mdw.core.LoadProfileType;
import com.energyict.mdw.core.LogBookType;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Path("/devicetypes")
public class DeviceTypeResource {

    private final DeviceConfigurationService deviceConfigurationService;

    @Inject
    public DeviceTypeResource(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceTypeInfos getAllDeviceTypes(@QueryParam("start") Integer start, @QueryParam("limit") Integer limit, @QueryParam("sortColumns") List<String> sortColumns) {
        DeviceTypeInfos deviceTypeInfos = new DeviceTypeInfos();
        deviceTypeInfos.deviceTypes = new ArrayList<>();
        Finder<DeviceType> deviceTypeFinder = deviceConfigurationService.allDeviceTypes().paged(start, limit);
        for (String sortColumn : sortColumns) {
            deviceTypeFinder.sorted(sortColumn, SortOrder.ASCENDING);
        }
        List<DeviceType> allDeviceTypes = deviceTypeFinder.find();

        for (DeviceType deviceType : allDeviceTypes) {
            deviceTypeInfos.deviceTypes.add(new DeviceTypeInfo(deviceType));
        }

        return deviceTypeInfos;
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteDeviceType(@PathParam("id") String name) throws SQLException, BusinessException {
        DeviceType deviceType = deviceConfigurationService.findDeviceType(name);
        if (deviceType == null) {
            throw new WebApplicationException("No device type with name " + name,
                    Response.status(Response.Status.NOT_FOUND).build());
        }

        deviceType.delete();
        return Response.ok().build();

    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public DeviceTypeInfo createDeviceType(DeviceTypeInfo deviceTypeInfo) {
        return new DeviceTypeInfo(null);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceTypeInfo findDeviceType(@PathParam("id") String name) {
        DeviceType deviceType = deviceConfigurationService.findDeviceType(name);
        if (deviceType == null) {
            throw new WebApplicationException("No device type with name " + name,
                    Response.status(Response.Status.NOT_FOUND).build());
        }
        return new DeviceTypeInfo(deviceType);
    }

    @GET
    @Path("/{id}/loadprofiletypes")
    @Produces(MediaType.APPLICATION_JSON)
    public List<LoadProfileTypeInfo> getLoadProfilesForDeviceType(@PathParam("id") String name) {
        DeviceType deviceType = deviceConfigurationService.findDeviceType(name);
        if (deviceType == null) {
            throw new WebApplicationException("No device type with name " + name,
                    Response.status(Response.Status.NOT_FOUND).build());
        }
        List<LoadProfileTypeInfo> loadProfileTypeInfos = new ArrayList<>();
        for (LoadProfileType loadProfileType : deviceType.getLoadProfileTypes()) {
            loadProfileTypeInfos.add(new LoadProfileTypeInfo(loadProfileType));
        }
        return loadProfileTypeInfos;
    }

    @GET
    @Path("/{id}/logbooktypes")
    @Produces(MediaType.APPLICATION_JSON)
    public List<LogBookTypeInfo> getLogBookTypesForDeviceType(@PathParam("id") String name) {
        DeviceType deviceType = deviceConfigurationService.findDeviceType(name);
        if (deviceType == null) {
            throw new WebApplicationException("No device type with name " + name,
                    Response.status(Response.Status.NOT_FOUND).build());
        }
        List<LogBookTypeInfo> logBookTypeInfos = new ArrayList<>();
        for (LogBookType logBookType : deviceType.getLogBookTypes()) {
            logBookTypeInfos.add(new LogBookTypeInfo(logBookType));
        }
        return logBookTypeInfos;
    }

    @GET
    @Path("/{id}/deviceconfigurations")
    @Produces(MediaType.APPLICATION_JSON)
    public List<DeviceConfigurationInfo> getDeviceConfigurationsForDeviceType(@PathParam("id") String name) {
        DeviceType deviceType = deviceConfigurationService.findDeviceType(name);
        if (deviceType == null) {
            throw new WebApplicationException("No device type with name " + name,
                    Response.status(Response.Status.NOT_FOUND).build());
        }
        List<DeviceConfigurationInfo> deviceConfigurationInfos = new ArrayList<>();
        for (DeviceConfiguration deviceConfiguration : deviceType.getConfigurations()) {
            deviceConfigurationInfos.add(new DeviceConfigurationInfo(deviceConfiguration));
        }
        return deviceConfigurationInfos;
    }

    @GET
    @Path("/{id}/registers")
    @Produces(MediaType.APPLICATION_JSON)
    public List<RegisterMappingInfo> getRegistersForDeviceType(@PathParam("id") String name) {
        DeviceType deviceType = deviceConfigurationService.findDeviceType(name);
        if (deviceType == null) {
            throw new WebApplicationException("No device type with name " + name,
                    Response.status(Response.Status.NOT_FOUND).build());
        }
        List<RegisterMappingInfo> registerMappingInfos = new ArrayList<>();
        for (RegisterMapping registerMapping : deviceType.getRegisterMappings()) {
            registerMappingInfos.add(new RegisterMappingInfo(registerMapping));
        }
        return registerMappingInfos;
    }

}
