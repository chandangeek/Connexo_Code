package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileType;
import com.energyict.mdc.device.config.LogBookType;
import com.energyict.mdc.device.config.RegisterMapping;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Path("/devicetypes")
public class DeviceTypeResource {

    private final DeviceConfigurationService deviceConfigurationService;

    @Inject
    public DeviceTypeResource(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getAllDeviceTypes(@BeanParam QueryParameters queryParameters) {
        List<DeviceTypeInfo> deviceTypeInfos = new ArrayList<>();
        Finder<DeviceType> deviceTypeFinder = deviceConfigurationService.findAllDeviceTypes();
        List<DeviceType> allDeviceTypes = deviceTypeFinder.paged(queryParameters.getStart(), queryParameters.getLimit()).find();
        for (DeviceType deviceType : allDeviceTypes) {
            deviceTypeInfos.add(new DeviceTypeInfo(deviceType));
        }

        return PagedInfoList.asJson("deviceTypes", deviceTypeInfos, queryParameters);
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteDeviceType(@PathParam("id") long id) throws SQLException, BusinessException {
        DeviceType deviceType = findDeviceTypeByNameOrThrowException(id);
        deviceType.delete();
        return Response.ok().build();

    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public DeviceTypeInfo createDeviceType(DeviceTypeInfo deviceTypeInfo) {
        DeviceType deviceType = this.deviceConfigurationService.newDeviceType(deviceTypeInfo.name, deviceTypeInfo.deviceProtocolInfo.name);
        deviceType.save();
        return new DeviceTypeInfo(deviceType);
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public DeviceTypeInfo updateDeviceType(@PathParam("id") long id, DeviceTypeInfo deviceTypeInfo) {
        DeviceType deviceType = findDeviceTypeByNameOrThrowException(id);
        deviceType.setName(deviceTypeInfo.name);
        try {
            deviceType.setDeviceProtocolPluggableClass(deviceTypeInfo.deviceProtocolInfo.name);
            deviceType.save();
            return new DeviceTypeInfo(deviceType);
        } catch (Exception e) {
            throw new WebApplicationException("failed to update device type "+deviceTypeInfo.id, e, Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).build());
        }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceTypeInfo findDeviceType(@PathParam("id") long id) {
        DeviceType deviceType = findDeviceTypeByNameOrThrowException(id);
        return new DeviceTypeInfo(deviceType);
    }

    @GET
    @Path("/{id}/loadprofiletypes")
    @Produces(MediaType.APPLICATION_JSON)
    public List<LoadProfileTypeInfo> getLoadProfilesForDeviceType(@PathParam("id") long id) {
        DeviceType deviceType = findDeviceTypeByNameOrThrowException(id);
        List<LoadProfileTypeInfo> loadProfileTypeInfos = new ArrayList<>();
        for (LoadProfileType loadProfileType : deviceType.getLoadProfileTypes()) {
            loadProfileTypeInfos.add(new LoadProfileTypeInfo(loadProfileType));
        }
        return loadProfileTypeInfos;
    }

    @GET
    @Path("/{id}/logbooktypes")
    @Produces(MediaType.APPLICATION_JSON)
    public List<LogBookTypeInfo> getLogBookTypesForDeviceType(@PathParam("id") long id) {
        DeviceType deviceType = findDeviceTypeByNameOrThrowException(id);
        List<LogBookTypeInfo> logBookTypeInfos = new ArrayList<>();
        for (LogBookType logBookType : deviceType.getLogBookTypes()) {
            logBookTypeInfos.add(new LogBookTypeInfo(logBookType));
        }
        return logBookTypeInfos;
    }

    @GET
    @Path("/{id}/deviceconfigurations")
    @Produces(MediaType.APPLICATION_JSON)
    public List<DeviceConfigurationInfo> getDeviceConfigurationsForDeviceType(@PathParam("id") long id) {
        DeviceType deviceType = findDeviceTypeByNameOrThrowException(id);
        List<DeviceConfigurationInfo> deviceConfigurationInfos = new ArrayList<>();
        for (DeviceConfiguration deviceConfiguration : deviceType.getConfigurations()) {
            deviceConfigurationInfos.add(new DeviceConfigurationInfo(deviceConfiguration));
        }
        return deviceConfigurationInfos;
    }

    @GET
    @Path("/{id}/registers")
    @Produces(MediaType.APPLICATION_JSON)
    public List<RegisterMappingInfo> getRegistersForDeviceType(@PathParam("id") long id, @QueryParam("available") String available) {
        DeviceType deviceType = findDeviceTypeByNameOrThrowException(id);
        List<RegisterMappingInfo> registerMappingInfos = new ArrayList<>();

        final List<RegisterMapping> registerMappings = new ArrayList<>();
        if (available==null || !Boolean.parseBoolean(available)) {
            registerMappings.addAll(deviceType.getRegisterMappings());
        } else {
            if (Boolean.parseBoolean(available)) {
                Set<Long> deviceTypeRegisterMappingIds = new HashSet<>();
                for (RegisterMapping registerMapping : deviceType.getRegisterMappings()) {
                    deviceTypeRegisterMappingIds.add(registerMapping.getId());
                }
                for (RegisterMapping registerMapping : deviceConfigurationService.findAllRegisterMappings()) {
                    if (!deviceTypeRegisterMappingIds.contains(registerMapping.getId())) {
                        registerMappings.add(registerMapping);
                    }
                }
            }
        }
        for (RegisterMapping registerMapping : registerMappings) {
            registerMappingInfos.add(new RegisterMappingInfo(registerMapping));
        }

        return registerMappingInfos;
    }

    @PUT
    @Path("/{id}/registers")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<RegisterMappingInfo> updateRegistersForDeviceType(@PathParam("id") long id, List<RegisterMappingInfo> registerMappingInfos) {
        DeviceType deviceType = findDeviceTypeByNameOrThrowException(id);
        updateRegisterMappings(deviceType, registerMappingInfos);
        deviceType.save();
        return getRegistersForDeviceType(id, null);
    }

    @POST
    @Path("/{id}/registers")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<RegisterMappingInfo> createRegistersForDeviceType(@PathParam("id") long id, RegisterMappingInfo registerMappingInfo) {
        DeviceType deviceType = findDeviceTypeByNameOrThrowException(id);
        linkRegisterMappingToDeviceType(deviceType, registerMappingInfo);
        deviceType.save();
        return getRegistersForDeviceType(id, null);
    }

    @DELETE
    @Path("/{id}/registers")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public RegisterMappingInfo deleteRegistersForDeviceType(@PathParam("id") long id, RegisterMappingInfo registerMappingInfo) {
        DeviceType deviceType = findDeviceTypeByNameOrThrowException(id);
        unlinkRegisterMappingFromDeviceType(deviceType, registerMappingInfo.id);
        deviceType.save();
        return registerMappingInfo;
    }


    private void updateRegisterMappings(DeviceType deviceType, List<RegisterMappingInfo> newRegisterMappings) {
        Map<Long, RegisterMappingInfo> newRegisterMappingsIdMap = asIdz(newRegisterMappings);
        for (RegisterMapping existingRegisterMapping : deviceType.getRegisterMappings()) {
            if (newRegisterMappingsIdMap.containsKey(Long.valueOf(existingRegisterMapping.getId()))) {
                // We don't update anything about RegisterMapping in the resource
                newRegisterMappingsIdMap.remove(Long.valueOf(existingRegisterMapping.getId()));
            } else {
                unlinkRegisterMappingFromDeviceType(deviceType, existingRegisterMapping.getId());
            }
        }

        for (RegisterMappingInfo registerMappingInfo : newRegisterMappingsIdMap.values()) {
            linkRegisterMappingToDeviceType(deviceType, registerMappingInfo);
        }
    }

    private void unlinkRegisterMappingFromDeviceType(DeviceType deviceType, long existingRegisterMappingId) {
        deviceType.removeRegisterMapping(getRegisterMappingById(deviceType, existingRegisterMappingId));
    }

    private void linkRegisterMappingToDeviceType(DeviceType deviceType, RegisterMappingInfo registerMappingInfo) {
        RegisterMapping registerMapping = this.deviceConfigurationService.findRegisterMapping(registerMappingInfo.id);
        if (registerMapping==null) {
            throw new WebApplicationException("No register mapping with id " + registerMappingInfo.id,
                    Response.status(Response.Status.BAD_REQUEST).build());

        }
        deviceType.addRegisterMapping(registerMapping);
    }

    private RegisterMapping getRegisterMappingById(DeviceType deviceType, long id) {
        for (RegisterMapping registerMapping : deviceType.getRegisterMappings()) {
            if (registerMapping.getId() == id) {
                return registerMapping;
            }
        }
        throw new WebApplicationException("This situation should never have happened: error looking up shadow in internal list: " + id,
                Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
    }

    private Map<Long, RegisterMappingInfo> asIdz(List<RegisterMappingInfo> registerMappingInfos) {
        Map<Long, RegisterMappingInfo> registerMappingIdMap = new HashMap<>();
        for (RegisterMappingInfo registerMappingInfo : registerMappingInfos) {
            registerMappingIdMap.put(registerMappingInfo.id, registerMappingInfo);
        }
        return registerMappingIdMap;
    }

    private DeviceType findDeviceTypeByNameOrThrowException(long id) {
        DeviceType deviceType = deviceConfigurationService.findDeviceType(id);
        if (deviceType == null) {
            throw new WebApplicationException("No device type with id " + id,
                    Response.Status.NOT_FOUND);
        }
        return deviceType;
    }

}