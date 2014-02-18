package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.services.DeviceConfigurationService;
import com.energyict.mdw.amr.RegisterMapping;
import com.energyict.mdw.amr.RegisterMappingFactory;
import com.energyict.mdw.core.DeviceConfiguration;
import com.energyict.mdw.core.DeviceType;
import com.energyict.mdw.core.LoadProfileType;
import com.energyict.mdw.core.LogBookType;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.shadow.DeviceTypeShadow;
import com.energyict.mdw.shadow.amr.RegisterMappingShadow;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final RegisterMappingFactory registerMappingFactory;

    @Inject
    public DeviceTypeResource(DeviceConfigurationService deviceConfigurationService, RegisterMappingFactory registerMappingFactory) {
        this.deviceConfigurationService = deviceConfigurationService;
        this.registerMappingFactory = registerMappingFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getAllDeviceTypes(@BeanParam QueryParameters queryParameters) {
        List<DeviceTypeInfo> deviceTypeInfos = new ArrayList<>();
        Finder<DeviceType> deviceTypeFinder = deviceConfigurationService.allDeviceTypes();
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
        return new DeviceTypeInfo(deviceConfigurationService.createDeviceType(deviceTypeInfo.name, deviceTypeInfo.deviceProtocolInfo.name));
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public DeviceTypeInfo updateDeviceType(@PathParam("id") long id, DeviceTypeInfo deviceTypeInfo) {
        DeviceType deviceType = findDeviceTypeByNameOrThrowException(id);
        DeviceTypeShadow shadow = deviceType.getShadow();
        shadow.setName(deviceTypeInfo.name);
        List<DeviceProtocolPluggableClass> deviceProtocolPluggableClasses = MeteringWarehouse.getCurrent().getProtocolPluggableService().findAllDeviceProtocolPluggableClasses();
        for (DeviceProtocolPluggableClass deviceProtocolPluggableClass : deviceProtocolPluggableClasses) {
            if (deviceProtocolPluggableClass.getName().equals(deviceTypeInfo.deviceProtocolInfo.name)) {
                shadow.setDeviceProtocolPluggableClassId(deviceProtocolPluggableClass.getId());
            }
        }
        try {
            deviceType.update(shadow);
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
                int[] existing = new int[deviceType.getRegisterMappings().size()];
                int index=0;
                for (RegisterMapping registerMapping : deviceType.getRegisterMappings()) {
                    existing[index++]=registerMapping.getId();
                }

                registerMappings.addAll(registerMappingFactory.findAllExcept(existing));
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

        return getRegistersForDeviceType(id, null);
    }

    @POST
    @Path("/{id}/registers")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<RegisterMappingInfo> createRegistersForDeviceType(@PathParam("id") long id, RegisterMappingInfo registerMappingInfo) {
        DeviceType deviceType = findDeviceTypeByNameOrThrowException(id);

        linkRegisterMappingToDeviceType(deviceType.getShadow(), registerMappingInfo);

        return getRegistersForDeviceType(id, null);
    }

    @DELETE
    @Path("/{id}/registers")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public RegisterMappingInfo deleteRegistersForDeviceType(@PathParam("id") long id, RegisterMappingInfo registerMappingInfo) {
        DeviceType deviceType = findDeviceTypeByNameOrThrowException(id);

        unlinkRegisterMappingFromDeviceType(deviceType.getShadow(), registerMappingInfo.id);

        return registerMappingInfo;
    }


    private void updateRegisterMappings(DeviceType deviceType, List<RegisterMappingInfo> newRegisterMappings) {
        DeviceTypeShadow deviceTypeShadow = deviceType.getShadow();
        Map<Long, RegisterMappingInfo> newRegisterMappingsIdMap = asIdz(newRegisterMappings);
        for (RegisterMapping existingRegisterMapping : deviceType.getRegisterMappings()) {
            if (newRegisterMappingsIdMap.containsKey(Long.valueOf(existingRegisterMapping.getId()))) {
                // We don't update anything about RegisterMapping in the resource
                newRegisterMappingsIdMap.remove(Long.valueOf(existingRegisterMapping.getId()));
            } else {
                unlinkRegisterMappingFromDeviceType(deviceTypeShadow, existingRegisterMapping.getId());
            }
        }

        for (RegisterMappingInfo registerMappingInfo : newRegisterMappingsIdMap.values()) {
            linkRegisterMappingToDeviceType(deviceTypeShadow, registerMappingInfo);
        }
    }

    private void unlinkRegisterMappingFromDeviceType(DeviceTypeShadow deviceTypeShadow, long existingRegisterMappingId) {
        deviceTypeShadow.getRegisterMappingShadows().remove(getRegisterMappingById(deviceTypeShadow.getRegisterMappingShadows(), (int)existingRegisterMappingId));
    }

    private void linkRegisterMappingToDeviceType(DeviceTypeShadow deviceTypeShadow, RegisterMappingInfo registerMappingInfo) {
        RegisterMapping registerMapping = registerMappingFactory.find((int) registerMappingInfo.id);
        if (registerMapping==null) {
            throw new WebApplicationException("No register mapping with id " + registerMappingInfo.id,
                    Response.status(Response.Status.BAD_REQUEST).build());

        }
        deviceTypeShadow.getRegisterMappingShadows().add(registerMapping.getShadow());
    }

    private RegisterMappingShadow getRegisterMappingById(List<RegisterMappingShadow> registerMappingShadows, int id) {
        for (RegisterMappingShadow registerMappingShadow : registerMappingShadows) {
            if (registerMappingShadow.getId()==id) {
                return registerMappingShadow;
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
