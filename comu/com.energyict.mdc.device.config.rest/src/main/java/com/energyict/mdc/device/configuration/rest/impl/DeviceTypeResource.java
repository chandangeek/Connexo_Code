package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.common.rest.JsonQueryFilter;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.*;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterMapping;
import com.energyict.mdc.masterdata.rest.RegisterMappingInfo;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.*;

@Path("/devicetypes")
public class DeviceTypeResource {
    private final ResourceHelper resourceHelper;
    private final MasterDataService masterDataService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final Provider<DeviceConfigurationResource> deviceConfigurationResourceProvider;
    private final Provider<LoadProfileTypeResource> loadProfileTypeResourceProvider;
    private final ProtocolPluggableService protocolPluggableService;
    private final Thesaurus thesaurus;

    @Inject
    public DeviceTypeResource(
            ResourceHelper resourceHelper,
            MasterDataService masterDataService,
            DeviceConfigurationService deviceConfigurationService,
            ProtocolPluggableService protocolPluggableService,
            Provider<DeviceConfigurationResource> deviceConfigurationResourceProvider,
            Provider<LoadProfileTypeResource> loadProfileTypeResourceProvider,
            Thesaurus thesaurus) {
        this.resourceHelper = resourceHelper;
        this.masterDataService = masterDataService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.protocolPluggableService = protocolPluggableService;
        this.loadProfileTypeResourceProvider = loadProfileTypeResourceProvider;
        this.deviceConfigurationResourceProvider = deviceConfigurationResourceProvider;
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


    @Path("/{id}/loadprofiletypes")
    public LoadProfileTypeResource getLoadProfileTypesResource() {
        return loadProfileTypeResourceProvider.get();
    }

    @GET
    @Path("/{id}/logbooktypes")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLogBookTypesForDeviceType(@PathParam("id") long id, @BeanParam QueryParameters queryParameters, @QueryParam("available") String available) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        List<LogBookType> resultLogBookTypes = deviceType.getLogBookTypes();
        if (available != null && Boolean.parseBoolean(available)) {
            resultLogBookTypes = findAllAvailableLogBookTypesForDeviceType(resultLogBookTypes);
        }
        return Response.ok(PagedInfoList.asJson("data",
                        LogBookTypeInfo.from(ListPager.of(resultLogBookTypes, new LogBookTypeComparator()).find()), queryParameters)).build();
    }

    private List<LogBookType> findAllAvailableLogBookTypesForDeviceType(List<LogBookType> registeredLogBookTypes) {
        List<LogBookType> allLogBookTypes = masterDataService.findAllLogBookTypes();
        Set<Long> registeredLogBookTypeIds = new HashSet<>(registeredLogBookTypes.size());
        for (LogBookType logBookType : registeredLogBookTypes) {
            registeredLogBookTypeIds.add(logBookType.getId());
        }
        Iterator<LogBookType> logBookTypeIterator = allLogBookTypes.iterator();
        while (logBookTypeIterator.hasNext()) {
            LogBookType logBookType = logBookTypeIterator.next();
            if (registeredLogBookTypeIds.contains(logBookType.getId())){
                logBookTypeIterator.remove();
            }
        }
        return allLogBookTypes;
    }

    @POST
    @Path("/{id}/logbooktypes")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addLogBookTypesForDeviceType(@PathParam("id") long id, List<Long> ids) {
        if (ids.isEmpty()) {
            throw new TranslatableApplicationException(thesaurus, MessageSeeds.NO_LOGBOOK_TYPE_ID_FOR_ADDING);
        }
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        List<LogBookType> logBookTypes = new ArrayList<>(ids.size());
        for (Long logBookTypeId : ids) {
            Optional<LogBookType> logBookTypeRef = masterDataService.findLogBookType(logBookTypeId);
            if (logBookTypeRef.isPresent()){
                logBookTypes.add(logBookTypeRef.get());
            }
        }
        for (LogBookType logBookType : logBookTypes) {
            deviceType.addLogBookType(logBookType);
        }
        return Response.ok(LogBookTypeInfo.from(logBookTypes)).build();
    }

    @DELETE
    @Path("/{id}/logbooktypes/{lbid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteLogbookTypeFromDeviceType(@PathParam("id") long id, @PathParam("lbid") long lbid) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        Optional<LogBookType> logBookTypeRef = masterDataService.findLogBookType(lbid);
        if(!logBookTypeRef.isPresent()){
            throw new TranslatableApplicationException(thesaurus, MessageSeeds.NO_LOGBOOK_TYPE_FOUND, lbid);
        }
        LogBookType logBookType = logBookTypeRef.get();
        deleteLogBookTypeFromChildConfigurations(deviceType, logBookType);
        deviceType.removeLogBookType(logBookType);
        return Response.ok().build();
    }

    private void deleteLogBookTypeFromChildConfigurations(DeviceType deviceType, LogBookType logBookType) {
        List<DeviceConfiguration> deviceConfigurations = deviceConfigurationService.findDeviceConfigurationsUsingDeviceType(deviceType).find();
        for (DeviceConfiguration deviceConfiguration : deviceConfigurations) {
            List<LogBookSpec> logBookSpecs = new ArrayList<>(deviceConfiguration.getLogBookSpecs());
            for (LogBookSpec logBookSpec : logBookSpecs) {
                if (logBookSpec.getLogBookType().getId() == logBookType.getId()){
                    deviceConfiguration.deleteLogBookSpec(logBookSpec);
                    break;
                }
            }
        }
    }

    @Path("/{deviceTypeId}/deviceconfigurations")
    public DeviceConfigurationResource getDeviceConfigurationResource() {
        return deviceConfigurationResourceProvider.get();
    }

    @GET
    @Path("/{id}/registertypes")
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getRegisterMappingsForDeviceType(@PathParam("id") long id, @BeanParam QueryParameters queryParameters, @BeanParam JsonQueryFilter availableFilter) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        String available = availableFilter.getFilterProperties().get("available");
        final List<RegisterMapping> registerMappings = new ArrayList<>();
        if (available == null || !Boolean.parseBoolean(available)) {
            registerMappings.addAll(ListPager.of(deviceType.getRegisterMappings(), new RegisterTypeComparator()).from(queryParameters).find());
        } else {
            String deviceConfiguationIdString = availableFilter.getFilterProperties().get("deviceconfigurationid");
            if(deviceConfiguationIdString!=null){
                findAllAvailableRegisterMappingsForDeviceConfiguration(deviceType, registerMappings, deviceConfiguationIdString);
            } else {
                findAllAvailableRegisterMappingsForDeviceType(queryParameters, deviceType, registerMappings);
            }

        }
        List<RegisterMappingInfo> registerMappingInfos = asInfoList(deviceType, registerMappings);
        return PagedInfoList.asJson("registerTypes", registerMappingInfos, queryParameters);
    }

    private void findAllAvailableRegisterMappingsForDeviceType(QueryParameters queryParameters, DeviceType deviceType, List<RegisterMapping> registerMappings) {
        Set<Long> deviceTypeRegisterMappingIds = asIds(deviceType.getRegisterMappings());
        for (RegisterMapping registerMapping : this.masterDataService.findAllRegisterMappings().from(queryParameters).find()) {
            if (!deviceTypeRegisterMappingIds.contains(registerMapping.getId())) {
                registerMappings.add(registerMapping);
            }
        }
    }

    private void findAllAvailableRegisterMappingsForDeviceConfiguration(DeviceType deviceType, List<RegisterMapping> registerMappings, String deviceConfiguationIdString) {
        int deviceConfigurationId = Integer.parseInt(deviceConfiguationIdString);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType ,deviceConfigurationId);
        registerMappings.addAll(deviceType.getRegisterMappings());
        Set<Long> unavailableRegisterMappingIds = new HashSet<>();
        for(RegisterSpec registerSpec: deviceConfiguration.getRegisterSpecs()){
            unavailableRegisterMappingIds.add(registerSpec.getRegisterMapping().getId());
        }
        for (Iterator<RegisterMapping> iterator = registerMappings.iterator(); iterator.hasNext(); ) {
            RegisterMapping next =  iterator.next();
            if(unavailableRegisterMappingIds.contains(next.getId())){
                iterator.remove();
            }
        }
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
        Optional<RegisterMapping> registerMapping = this.masterDataService.findRegisterMapping(registerMappingId);
        if (!registerMapping.isPresent()) {
            throw new WebApplicationException("No register mapping with id " + registerMappingId,
                    Response.status(Response.Status.BAD_REQUEST).build());

        }
        deviceType.addRegisterMapping(registerMapping.get());
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
            boolean isLinkedByDeviceType = !deviceConfigurationService.findDeviceTypesUsingRegisterMapping(registerMapping).isEmpty();
            boolean isLinkedByActiveRegisterSpec = !deviceConfigurationService.findActiveRegisterSpecsByDeviceTypeAndRegisterMapping(deviceType, registerMapping).isEmpty();
            boolean isLinkedByInactiveRegisterSpec = !deviceConfigurationService.findInactiveRegisterSpecsByDeviceTypeAndRegisterMapping(deviceType, registerMapping).isEmpty();
            registerMappingInfos.add(new RegisterMappingInfo(registerMapping, isLinkedByDeviceType, isLinkedByActiveRegisterSpec, isLinkedByInactiveRegisterSpec));
        }
        return registerMappingInfos;
    }

}