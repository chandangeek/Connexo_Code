package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.common.services.ListFinder;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileType;
import com.energyict.mdc.device.config.LogBookType;
import com.energyict.mdc.device.config.RegisterMapping;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
        List<DeviceType> allDeviceTypes = deviceTypeFinder.from(queryParameters).find();
        for (DeviceType deviceType : allDeviceTypes) {
            deviceTypeInfos.add(new DeviceTypeInfo(deviceType));
        }

        return PagedInfoList.asJson("deviceTypes", deviceTypeInfos, queryParameters);
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteDeviceType(@PathParam("id") long id) throws SQLException, BusinessException {
        DeviceType deviceType = findDeviceTypeByIdOrThrowException(id);
        deviceType.delete();
        return Response.ok().build();

    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public DeviceTypeInfo createDeviceType(DeviceTypeInfo deviceTypeInfo) {
        DeviceType deviceType = deviceConfigurationService.newDeviceType(deviceTypeInfo.name, deviceTypeInfo.deviceProtocolInfo.name);
        deviceType.save();
        return new DeviceTypeInfo(deviceType);
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public DeviceTypeInfo updateDeviceType(@PathParam("id") long id, DeviceTypeInfo deviceTypeInfo) {
        DeviceType deviceType = findDeviceTypeByIdOrThrowException(id);
        deviceType.setName(deviceTypeInfo.name);
        deviceType.setDeviceProtocolPluggableClass(deviceTypeInfo.deviceProtocolInfo.name);
        if (deviceTypeInfo.registerMappings != null) {
            updateRegisterMappingAssociations(deviceType, deviceTypeInfo.registerMappings);
        }
        try {
            deviceType.save();
            return new DeviceTypeInfo(deviceType);
        } catch (Exception e) {
            throw new WebApplicationException("failed to update device type " + deviceTypeInfo.id, e, Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).build());
        }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceTypeInfo findDeviceType(@PathParam("id") long id) {
        DeviceType deviceType = findDeviceTypeByIdOrThrowException(id);
        return new DeviceTypeInfo(deviceType, deviceType.getRegisterMappings());
    }

    @GET
    @Path("/{id}/loadprofiletypes")
    @Produces(MediaType.APPLICATION_JSON)
    public List<LoadProfileTypeInfo> getLoadProfilesForDeviceType(@PathParam("id") long id) {
        DeviceType deviceType = findDeviceTypeByIdOrThrowException(id);
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
        DeviceType deviceType = findDeviceTypeByIdOrThrowException(id);
        List<LogBookTypeInfo> logBookTypeInfos = new ArrayList<>();
        for (LogBookType logBookType : ListFinder.of(deviceType.getLogBookTypes(), new LogBookTypeComparator()).find()) {
            logBookTypeInfos.add(new LogBookTypeInfo(logBookType));
        }
        return logBookTypeInfos;
    }

    @GET
    @Path("/{id}/deviceconfigurations")
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getDeviceConfigurationsForDeviceType(@PathParam("id") long id, @BeanParam QueryParameters queryParameters) {
        DeviceType deviceType = findDeviceTypeByIdOrThrowException(id);
        List<DeviceConfiguration> deviceConfigurations =
                deviceConfigurationService.
                        findDeviceConfigurationsUsingDeviceType(deviceType).
                        from(queryParameters).
                        find();
        List<DeviceConfigurationInfo> deviceConfigurationInfos = new ArrayList<>();
        for (DeviceConfiguration deviceConfiguration : deviceConfigurations) {
            deviceConfigurationInfos.add(new DeviceConfigurationInfo(deviceConfiguration));
        }
        return PagedInfoList.asJson("deviceConfigurations", deviceConfigurationInfos, queryParameters);
    }

    @GET
    @Path("/{id}/deviceconfigurations/{deviceConfigurationId}")
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceConfigurationInfo getDeviceConfigurationsById(@PathParam("id") long id, @PathParam("deviceConfigurationId") long deviceConfigurationId) {
        DeviceType deviceType = findDeviceTypeByIdOrThrowException(id);
        for (DeviceConfiguration deviceConfiguration : deviceType.getConfigurations()) {
            if (deviceConfiguration.getId()==deviceConfigurationId) {
                return new DeviceConfigurationInfo(deviceConfiguration);
            }
        }
        throw new WebApplicationException("No such device configuration for the device type", Response.status(Response.Status.NOT_FOUND).entity("No such device configuration for the device type").build());
    }

    @POST
    @Path("/{id}/deviceconfigurations")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceConfigurationInfo createDeviceConfiguration(@PathParam("id") long id, DeviceConfigurationInfo deviceConfigurationInfo) {
        DeviceType deviceType = findDeviceTypeByIdOrThrowException(id);
        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration(deviceConfigurationInfo.name).description(deviceConfigurationInfo.description).add();
        return new DeviceConfigurationInfo(deviceConfiguration);
    }

    @GET
    @Path("/{id}/registertypes")
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getRegisterMappingsForDeviceType(@PathParam("id") long id, @QueryParam("available") String available, @BeanParam QueryParameters queryParameters) {
        DeviceType deviceType = findDeviceTypeByIdOrThrowException(id);
        List<RegisterMappingInfo> registerMappingInfos = new ArrayList<>();

        final List<RegisterMapping> registerMappings = new ArrayList<>();
        if (available == null || !Boolean.parseBoolean(available)) {
            registerMappings.addAll(ListFinder.of(deviceType.getRegisterMappings(), new RegisterTypeComparator()).from(queryParameters).find());
        } else {
            Set<Long> deviceTypeRegisterMappingIds = asIds(deviceType.getRegisterMappings());
            for (RegisterMapping registerMapping : this.deviceConfigurationService.findAllRegisterMappings().from(queryParameters).find()) {
                if (!deviceTypeRegisterMappingIds.contains(registerMapping.getId())) {
                    registerMappings.add(registerMapping);
                }
            }
        }
        for (RegisterMapping registerMapping : registerMappings) {
            boolean isLinkedByRegisterSpec = !deviceConfigurationService.findRegisterSpecsByDeviceTypeAndRegisterMapping(deviceType, registerMapping).isEmpty();
            registerMappingInfos.add(new RegisterMappingInfo(registerMapping, isLinkedByRegisterSpec));
        }

        return PagedInfoList.asJson("registerTypes", registerMappingInfos, queryParameters);
    }

    @POST
    @Path("/{id}/registertypes/{rmId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<RegisterMappingInfo> linkRegisterMappingToDeviceType(@PathParam("id") long id, @PathParam("rmId") long rmId) {
        DeviceType deviceType = findDeviceTypeByIdOrThrowException(id);

        linkRegisterMappingToDeviceType(deviceType, rmId);

        deviceType = findDeviceTypeByIdOrThrowException(id);

        List<RegisterMappingInfo> registerMappingInfos = new ArrayList<>();
        for (RegisterMapping registerMapping : deviceType.getRegisterMappings()) {
            boolean isLinkedByRegisterSpec = !deviceConfigurationService.findRegisterSpecsByDeviceTypeAndRegisterMapping(deviceType, registerMapping).isEmpty();
            registerMappingInfos.add(new RegisterMappingInfo(registerMapping, isLinkedByRegisterSpec));
        }

        return registerMappingInfos;
    }

    @DELETE
    @Path("/{id}/registertypes/{rmId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response unlinkRegisterMappingFromDeviceType(@PathParam("id") long id, @PathParam("rmId") long registerMappingId) {
        DeviceType deviceType = findDeviceTypeByIdOrThrowException(id);
        if (getRegisterMappingById(deviceType.getRegisterMappings(), registerMappingId)==null) {
            String message = "No register type with id " + registerMappingId + " configured on device " + id;
            throw new WebApplicationException(message,
                    Response.status(Response.Status.BAD_REQUEST).entity(message).build());
        }

        unlinkRegisterMappingFromDeviceType(deviceType, registerMappingId);

        return Response.ok().build();
    }


    private void updateRegisterMappingAssociations(DeviceType deviceType, List<RegisterMappingInfo> newRegisterMappings) {
        Set<Long> newRegisterMappingsIds = asIdz(newRegisterMappings);
        Set<Long> existingRegisterMappingsIds = asIds(deviceType.getRegisterMappings());

        List<Long> toBeDeleted = new ArrayList<>(existingRegisterMappingsIds);
        toBeDeleted.removeAll(newRegisterMappingsIds);
        for (Long toBeDeletedId : toBeDeleted) {
            unlinkRegisterMappingFromDeviceType(deviceType, toBeDeletedId);
        }

        List<Long> toBeCreated = new ArrayList<>(newRegisterMappingsIds);
        toBeCreated.removeAll(existingRegisterMappingsIds);
        for (Long toBeCreatedId : toBeCreated) {
            linkRegisterMappingToDeviceType(deviceType, toBeCreatedId);
        }
    }

    private void unlinkRegisterMappingFromDeviceType(DeviceType deviceType, long existingRegisterMappingId) {
        deviceType.removeRegisterMapping(getRegisterMappingById(deviceType.getRegisterMappings(), existingRegisterMappingId));
    }

    private void linkRegisterMappingToDeviceType(DeviceType deviceType, long registerMappingId) {
        RegisterMapping registerMapping = this.deviceConfigurationService.findRegisterMapping(registerMappingId);
        if (registerMapping==null) {
            throw new WebApplicationException("No register mapping with id " + registerMappingId,
                    Response.status(Response.Status.BAD_REQUEST).build());

        }
        deviceType.addRegisterMapping(registerMapping);
    }

    private RegisterMapping getRegisterMappingById(List<RegisterMapping> registerMappingShadows, long id) {
        for (RegisterMapping registerMappingShadow : registerMappingShadows) {
            if (registerMappingShadow.getId() == id) {
                return registerMappingShadow;
            }
        }
        return null;
    }

    private Set<Long> asIdz(List<RegisterMappingInfo> registerMappingInfos) {
        Set<Long> registerMappingIdList = new HashSet<>();
        for (RegisterMappingInfo registerMappingInfo : registerMappingInfos) {
            registerMappingIdList.add(registerMappingInfo.id);
        }
        return registerMappingIdList;
    }

    private Set<Long> asIds(List<RegisterMapping> registerMappingShadows) {
        Set<Long> registerMappingIdList = new HashSet<>();
        for (RegisterMapping registerMappingInfo : registerMappingShadows) {
            registerMappingIdList.add(registerMappingInfo.getId());
        }
        return registerMappingIdList;
    }

    private DeviceType findDeviceTypeByIdOrThrowException(long id) {
        DeviceType deviceType = deviceConfigurationService.findDeviceType(id);
        if (deviceType == null) {
            throw new WebApplicationException("No device type with id " + id, Response.Status.NOT_FOUND);
        }
        return deviceType;
    }

}