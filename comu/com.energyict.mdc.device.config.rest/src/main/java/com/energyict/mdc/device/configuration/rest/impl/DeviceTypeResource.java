package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.common.rest.JsonQueryFilter;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.MeasurementType;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.masterdata.rest.RegisterTypeInfo;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.Checks;
import com.google.common.base.Optional;

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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
        Optional<DeviceProtocolPluggableClass> deviceProtocolPluggableClass = protocolPluggableService.findDeviceProtocolPluggableClassByName(deviceTypeInfo.deviceProtocolPluggableClassName);
        if (Checks.is(deviceTypeInfo.deviceProtocolPluggableClassName).emptyOrOnlyWhiteSpace()) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_IS_REQUIRED, DeviceTypeInfo.COMMUNICATION_PROTOCOL_NAME,deviceTypeInfo.deviceProtocolPluggableClassName);
        }
        if (!deviceProtocolPluggableClass.isPresent()) {
            throw new LocalizedFieldValidationException(MessageSeeds.PROTOCOL_INVALID_NAME, DeviceTypeInfo.COMMUNICATION_PROTOCOL_NAME, deviceTypeInfo.deviceProtocolPluggableClassName);
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
        deviceType.setDeviceProtocolPluggableClass(deviceTypeInfo.deviceProtocolPluggableClassName);
        if (deviceTypeInfo.registerTypes != null) {
            updateRegisterTypeAssociations(deviceType, deviceTypeInfo.registerTypes);
        }
        deviceType.save();
        return DeviceTypeInfo.from(deviceType);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceTypeInfo findDeviceType(@PathParam("id") long id) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        return DeviceTypeInfo.from(deviceType, deviceType.getRegisterTypes());
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
    public PagedInfoList getRegisterTypesForDeviceType(@PathParam("id") long id, @BeanParam QueryParameters queryParameters, @BeanParam JsonQueryFilter availableFilter) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        String available = availableFilter.getFilterProperties().get("available");
        final List<RegisterType> registerTypes = new ArrayList<>();
        if (available == null || !Boolean.parseBoolean(available)) {
            registerTypes.addAll(ListPager.of(deviceType.getRegisterTypes(), new RegisterTypeComparator()).from(queryParameters).find());
        } else {
            String deviceConfiguationIdString = availableFilter.getFilterProperties().get("deviceconfigurationid");
            if(deviceConfiguationIdString!=null){
                findAllAvailableRegisterTypesForDeviceConfiguration(deviceType, registerTypes, deviceConfiguationIdString);
            } else {
                findAllAvailableRegisterTypesForDeviceType(queryParameters, deviceType, registerTypes);
            }

        }
        List<RegisterTypeInfo> registerTypeInfos = asInfoList(deviceType, registerTypes);
        return PagedInfoList.asJson("registerTypes", registerTypeInfos, queryParameters);
    }

    private void findAllAvailableRegisterTypesForDeviceType(QueryParameters queryParameters, DeviceType deviceType, List<RegisterType> registerTypes) {
        Set<Long> deviceTypeRegisterTypeIds = asIds(deviceType.getRegisterTypes());
        for (RegisterType registerType : this.masterDataService.findAllRegisterTypes().from(queryParameters).find()) {
            if (!deviceTypeRegisterTypeIds.contains(registerType.getId())) {
                registerTypes.add(registerType);
            }
        }
    }

    private void findAllAvailableRegisterTypesForDeviceConfiguration(DeviceType deviceType, List<RegisterType> registerTypes, String deviceConfiguationIdString) {
        int deviceConfigurationId = Integer.parseInt(deviceConfiguationIdString);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType ,deviceConfigurationId);
        registerTypes.addAll(deviceType.getRegisterTypes());
        Set<Long> unavailableRegisterTypeIds = new HashSet<>();
        for(RegisterSpec registerSpec: deviceConfiguration.getRegisterSpecs()){
            unavailableRegisterTypeIds.add(registerSpec.getRegisterType().getId());
        }
        for (Iterator<RegisterType> iterator = registerTypes.iterator(); iterator.hasNext(); ) {
            MeasurementType next =  iterator.next();
            if(unavailableRegisterTypeIds.contains(next.getId())){
                iterator.remove();
            }
        }
    }

    @POST
    @Path("/{id}/registertypes/{rmId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<RegisterTypeInfo> linkRegisterTypesToDeviceType(@PathParam("id") long id, @PathParam("rmId") long rmId) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);

        linkRegisterTypeToDeviceType(deviceType, rmId);

        deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);

        return asInfoList(deviceType, deviceType.getRegisterTypes());
    }

    @DELETE
    @Path("/{id}/registertypes/{rmId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response unlinkRegisterTypesFromDeviceType(@PathParam("id") long id, @PathParam("rmId") long registerTypeId) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        if (getRegisterTypeById(deviceType.getRegisterTypes(), registerTypeId)==null) {
            String message = "No register type with id " + registerTypeId + " configured on device " + id;
            throw new WebApplicationException(message,
                    Response.status(Response.Status.BAD_REQUEST).entity(message).build());
        }

        unlinkRegisterTypeFromDeviceType(deviceType, registerTypeId);

        return Response.ok().build();
    }


    private void updateRegisterTypeAssociations(DeviceType deviceType, List<RegisterTypeInfo> newRegisterTypeInfos) {
        Set<Long> newRegisterTypeIds = asIdz(newRegisterTypeInfos);
        Set<Long> existingRegisterTypeIds = asIds(deviceType.getRegisterTypes());

        List<Long> toBeDeleted = new ArrayList<>(existingRegisterTypeIds);
        toBeDeleted.removeAll(newRegisterTypeIds);
        for (Long toBeDeletedId : toBeDeleted) {
            unlinkRegisterTypeFromDeviceType(deviceType, toBeDeletedId);
        }

        List<Long> toBeCreated = new ArrayList<>(newRegisterTypeIds);
        toBeCreated.removeAll(existingRegisterTypeIds);
        for (Long toBeCreatedId : toBeCreated) {
            linkRegisterTypeToDeviceType(deviceType, toBeCreatedId);
        }
    }

    private void unlinkRegisterTypeFromDeviceType(DeviceType deviceType, long existingRegisterTypeId) {
        deviceType.removeRegisterType(getRegisterTypeById(deviceType.getRegisterTypes(), existingRegisterTypeId));
    }

    private void linkRegisterTypeToDeviceType(DeviceType deviceType, long registerTypeId) {
        Optional<RegisterType> registerType = this.masterDataService.findRegisterType(registerTypeId);
        if (!registerType.isPresent()) {
            throw new WebApplicationException("No register mapping with id " + registerTypeId,
                    Response.status(Response.Status.BAD_REQUEST).build());

        }
        deviceType.addRegisterType(registerType.get());
    }

    private RegisterType getRegisterTypeById(List<RegisterType> registerTypes, long id) {
        for (RegisterType registerType : registerTypes) {
            if (registerType.getId() == id) {
                return registerType;
            }
        }
        return null;
    }

    private Set<Long> asIdz(List<RegisterTypeInfo> registerTypeInfos) {
        Set<Long> registerTypeIds = new HashSet<>();
        for (RegisterTypeInfo registerTypeInfo : registerTypeInfos) {
            registerTypeIds.add(registerTypeInfo.id);
        }
        return registerTypeIds;
    }

    private Set<Long> asIds(List<? extends HasId> hasIdList) {
        Set<Long> idList = new HashSet<>();
        for (HasId hasId : hasIdList) {
            idList.add(hasId.getId());
        }
        return idList;
    }

    private List<RegisterTypeInfo> asInfoList(DeviceType deviceType, List<RegisterType> registerTypes) {
        List<RegisterTypeInfo> registerTypeInfos = new ArrayList<>();
        for (RegisterType registerType : registerTypes) {
            boolean isLinkedByDeviceType = !deviceConfigurationService.findDeviceTypesUsingRegisterType(registerType).isEmpty();
            boolean isLinkedByActiveRegisterSpec = !deviceConfigurationService.findActiveRegisterSpecsByDeviceTypeAndRegisterType(deviceType, registerType).isEmpty();
            boolean isLinkedByInactiveRegisterSpec = !deviceConfigurationService.findInactiveRegisterSpecsByDeviceTypeAndRegisterType(deviceType, registerType).isEmpty();
            registerTypeInfos.add(new RegisterTypeInfo(registerType, isLinkedByDeviceType, isLinkedByActiveRegisterSpec, isLinkedByInactiveRegisterSpec));
        }
        return registerTypeInfos;
    }

}