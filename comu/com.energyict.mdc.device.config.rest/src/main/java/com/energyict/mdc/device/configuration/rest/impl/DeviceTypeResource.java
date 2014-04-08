package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.google.common.base.Optional;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/devicetypes")
public class DeviceTypeResource {
    private final ResourceHelper resourceHelper;
    private final DeviceConfigurationService deviceConfigurationService;
    private final Provider<DeviceConfigurationResource> deviceConfigurationResourceProvider;
    private final ProtocolPluggableService protocolPluggableService;
    private final Thesaurus thesaurus;

    @Inject
    public DeviceTypeResource(ResourceHelper resourceHelper, DeviceConfigurationService deviceConfigurationService, Provider<DeviceConfigurationResource> deviceConfigurationResourceProvider, ProtocolPluggableService protocolPluggableService, Thesaurus thesaurus) {
        this.resourceHelper = resourceHelper;
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceConfigurationResourceProvider = deviceConfigurationResourceProvider;
        this.protocolPluggableService = protocolPluggableService;
        this.thesaurus = thesaurus;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getAllDeviceTypes(@BeanParam QueryParameters queryParameters) {
        Finder<DeviceType> deviceTypeFinder = deviceConfigurationService.findAllDeviceTypes();
        List<DeviceType> allDeviceTypes = deviceTypeFinder.from(queryParameters).find();
        List<DeviceTypeInfo> deviceTypeInfos = DeviceTypeInfo.from(allDeviceTypes);
        return PagedInfoList.asJson("deviceTypes", deviceTypeInfos, queryParameters);
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteDeviceType(@PathParam("id") long id) throws SQLException, BusinessException {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        deviceType.delete();
        return Response.ok().build();

    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public DeviceTypeInfo createDeviceType(DeviceTypeInfo deviceTypeInfo) {
        Optional<DeviceProtocolPluggableClass> deviceProtocolPluggableClass = protocolPluggableService.findDeviceProtocolPluggableClassByName(deviceTypeInfo.communicationProtocolName);
        if (!deviceProtocolPluggableClass.isPresent()) {
            throw new LocalizedFieldValidationException(thesaurus, MessageSeeds.PROTOCOL_INVALID_NAME, DeviceTypeInfo.COMMUNICATION_PROTOCOL_NAME, "name", deviceTypeInfo.communicationProtocolName);
        }
        DeviceType deviceType = deviceConfigurationService.newDeviceType(deviceTypeInfo.name, deviceProtocolPluggableClass.get());
        deviceType.save();
        return DeviceTypeInfo.from(deviceType);
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public DeviceTypeInfo updateDeviceType(@PathParam("id") long id, DeviceTypeInfo deviceTypeInfo) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        deviceType.setName(deviceTypeInfo.name);
        deviceType.setDeviceProtocolPluggableClass(deviceTypeInfo.communicationProtocolName);
        if (deviceTypeInfo.registerMappings != null) {
            updateRegisterMappingAssociations(deviceType, deviceTypeInfo.registerMappings);
        }
        deviceType.save();
        return DeviceTypeInfo.from(deviceType);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceTypeInfo findDeviceType(@PathParam("id") long id) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        return DeviceTypeInfo.from(deviceType, deviceType.getRegisterMappings());
    }

    @GET
    @Path("/{id}/loadprofiletypes")
    @Produces(MediaType.APPLICATION_JSON)
    public List<LoadProfileTypeInfo> getLoadProfilesForDeviceType(@PathParam("id") long id) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        return LoadProfileTypeInfo.from(deviceType.getLoadProfileTypes());
    }

    @GET
    @Path("/{id}/logbooktypes")
    @Produces(MediaType.APPLICATION_JSON)
    public List<LogBookTypeInfo> getLogBookTypesForDeviceType(@PathParam("id") long id) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        return LogBookTypeInfo.from(ListPager.of(deviceType.getLogBookTypes(), new LogBookTypeComparator()).find());
    }

    @Path("/{deviceTypeId}/deviceconfigurations")
    public DeviceConfigurationResource getDeviceConfigurationResource() {
        return deviceConfigurationResourceProvider.get();
    }

    @GET
    @Path("/{id}/registertypes")
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getRegisterMappingsForDeviceType(@PathParam("id") long id, @QueryParam("available") String available, @BeanParam QueryParameters queryParameters) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);

        final List<RegisterMapping> registerMappings = new ArrayList<>();
        if (available == null || !Boolean.parseBoolean(available)) {
            registerMappings.addAll(ListPager.of(deviceType.getRegisterMappings(), new RegisterTypeComparator()).from(queryParameters).find());
        } else {
            Set<Long> deviceTypeRegisterMappingIds = asIds(deviceType.getRegisterMappings());
            for (RegisterMapping registerMapping : this.deviceConfigurationService.findAllRegisterMappings().from(queryParameters).find()) {
                if (!deviceTypeRegisterMappingIds.contains(registerMapping.getId())) {
                    registerMappings.add(registerMapping);
                }
            }
        }

        List<RegisterMappingInfo> registerMappingInfos = asInfoList(deviceType, registerMappings);

        return PagedInfoList.asJson("registerTypes", registerMappingInfos, queryParameters);
    }

    @POST
    @Path("/{id}/registertypes/{rmId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<RegisterMappingInfo> linkRegisterMappingToDeviceType(@PathParam("id") long id, @PathParam("rmId") long rmId) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);

        linkRegisterMappingToDeviceType(deviceType, rmId);

        deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);

        return asInfoList(deviceType, deviceType.getRegisterMappings());
    }

    @DELETE
    @Path("/{id}/registertypes/{rmId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response unlinkRegisterMappingFromDeviceType(@PathParam("id") long id, @PathParam("rmId") long registerMappingId) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
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

    private List<RegisterMappingInfo> asInfoList(DeviceType deviceType, List<RegisterMapping> registerMappings) {
        List<RegisterMappingInfo> registerMappingInfos = new ArrayList<>();
        for (RegisterMapping registerMapping : registerMappings) {
            boolean isLinkedByActiveRegisterSpec = !deviceConfigurationService.findActiveRegisterSpecsByDeviceTypeAndRegisterMapping(deviceType, registerMapping).isEmpty();
            boolean isLinkedByInactiveRegisterSpec = !deviceConfigurationService.findInactiveRegisterSpecsByDeviceTypeAndRegisterMapping(deviceType, registerMapping).isEmpty();
            registerMappingInfos.add(new RegisterMappingInfo(registerMapping, isLinkedByActiveRegisterSpec, isLinkedByInactiveRegisterSpec));
        }
        return registerMappingInfos;
    }

}