package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.RestValidationBuilder;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.IncompatibleDeviceLifeCycleChangeException;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.device.config.security.Privileges;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.rest.info.DeviceLifeCycleInfo;
import com.energyict.mdc.device.lifecycle.config.rest.info.DeviceLifeCycleStateInfo;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.masterdata.rest.RegisterTypeInfo;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import javax.annotation.security.RolesAllowed;
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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

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
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_TYPE, Privileges.VIEW_DEVICE_TYPE})
    public PagedInfoList getAllDeviceTypes(@BeanParam JsonQueryParameters queryParameters) {
        Finder<DeviceType> deviceTypeFinder = deviceConfigurationService.findAllDeviceTypes();
        List<DeviceType> allDeviceTypes = deviceTypeFinder.from(queryParameters).find();
        List<DeviceTypeInfo> deviceTypeInfos = DeviceTypeInfo.from(allDeviceTypes);
        return PagedInfoList.fromPagedList("deviceTypes", deviceTypeInfos, queryParameters);
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_TYPE)
    public Response deleteDeviceType(@PathParam("id") long id) throws SQLException, BusinessException {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        deviceType.delete();
        return Response.ok().build();

    }

    @POST
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_TYPE)
    public DeviceTypeInfo createDeviceType(DeviceTypeInfo deviceTypeInfo) {
        Optional<DeviceProtocolPluggableClass> deviceProtocolPluggableClass = protocolPluggableService.findDeviceProtocolPluggableClassByName(deviceTypeInfo.deviceProtocolPluggableClassName);
        Optional<DeviceLifeCycle> deviceLifeCycleRef = deviceTypeInfo.deviceLifeCycleId != null ? resourceHelper.findDeviceLifeCycleById(deviceTypeInfo.deviceLifeCycleId) : Optional.empty();
        DeviceType deviceType = deviceConfigurationService.newDeviceType(deviceTypeInfo.name,
                deviceProtocolPluggableClass.isPresent() ? deviceProtocolPluggableClass.get() : null,
                deviceLifeCycleRef.isPresent() ? deviceLifeCycleRef.get() : null);
        deviceType.save();
        return DeviceTypeInfo.from(deviceType);
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_TYPE)
    public Response updateDeviceType(@PathParam("id") long id, DeviceTypeInfo deviceTypeInfo) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        deviceType.setName(deviceTypeInfo.name);
        deviceType.setDeviceProtocolPluggableClass(deviceTypeInfo.deviceProtocolPluggableClassName);
        if (deviceTypeInfo.registerTypes != null) {
            updateRegisterTypeAssociations(deviceType, deviceTypeInfo.registerTypes);
        }
        if (deviceTypeInfo.deviceLifeCycleId != null && (deviceType.getConfigurations().isEmpty() ||
                deviceType.getConfigurations().stream().noneMatch(conf -> conf.isActive()))){
            DeviceLifeCycle targetDeviceLifeCycle = resourceHelper.findDeviceLifeCycleByIdOrThrowException(deviceTypeInfo.deviceLifeCycleId);
            try {
                deviceConfigurationService.changeDeviceLifeCycle(deviceType, targetDeviceLifeCycle);
            } catch (IncompatibleDeviceLifeCycleChangeException mappingEx){
                DeviceLifeCycle oldDeviceLifeCycle = deviceType.getDeviceLifeCycle();
                ChangeDeviceLifeCycleInfo info = getChangeDeviceLifeCycleFailInfo(mappingEx, oldDeviceLifeCycle, targetDeviceLifeCycle);
                return Response.status(Response.Status.BAD_REQUEST).entity(info).build();
            }
        }
        deviceType.save();
        return Response.ok(DeviceTypeInfo.from(deviceType)).build();
    }

    private ChangeDeviceLifeCycleInfo getChangeDeviceLifeCycleFailInfo(IncompatibleDeviceLifeCycleChangeException lifeCycleChangeError, DeviceLifeCycle currentDeviceLifeCycle, DeviceLifeCycle targetDeviceLifeCycle) {
        ChangeDeviceLifeCycleInfo info = new ChangeDeviceLifeCycleInfo();
        info.success = false;
        String errorMessage = thesaurus.getString(MessageSeeds.UNABLE_TO_CHANGE_DEVICE_LIFE_CYCLE.getKey(), MessageSeeds.UNABLE_TO_CHANGE_DEVICE_LIFE_CYCLE.getDefaultFormat());
        info.message = new MessageFormat(errorMessage).format(new Object[]{targetDeviceLifeCycle.getName()}, new StringBuffer(), null).toString();
        info.currentDeviceLifeCycle = new DeviceLifeCycleInfo(currentDeviceLifeCycle);
        info.targetDeviceLifeCycle = new DeviceLifeCycleInfo(targetDeviceLifeCycle);
        info.notMappableStates = lifeCycleChangeError.getMissingStates()
                .stream()
                .map(state -> new DeviceLifeCycleStateInfo(thesaurus, state))
                .collect(Collectors.toList());
        return info;
    }

    @PUT
    @Path("/{id}/devicelifecycle")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_TYPE)
    public Response updateDeviceLifeCycleForDeviceType(@PathParam("id") long id, ChangeDeviceLifeCycleInfo info) {
        DeviceType deviceType = resourceHelper.findAndLockDeviceType(id, info.version);
        DeviceLifeCycle oldDeviceLifeCycle = deviceType.getDeviceLifeCycle();
        new RestValidationBuilder()
                .isCorrectId(info.targetDeviceLifeCycle != null ? info.targetDeviceLifeCycle.id : null, "deviceLifeCycleId")
                .validate();
        DeviceLifeCycle targetDeviceLifeCycle = resourceHelper.findDeviceLifeCycleByIdOrThrowException(info.targetDeviceLifeCycle.id);
        try {
            deviceConfigurationService.changeDeviceLifeCycle(deviceType, targetDeviceLifeCycle);
        } catch (IncompatibleDeviceLifeCycleChangeException mappingEx){
            info = getChangeDeviceLifeCycleFailInfo(mappingEx, oldDeviceLifeCycle, targetDeviceLifeCycle);
            return Response.status(Response.Status.BAD_REQUEST).entity(info).build();
        }
        return Response.ok(DeviceTypeInfo.from(deviceType)).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_TYPE, Privileges.VIEW_DEVICE_TYPE})
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
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_TYPE, Privileges.VIEW_DEVICE_TYPE})
    public PagedInfoList getLogBookTypesForDeviceType(@PathParam("id") long id, @BeanParam JsonQueryParameters queryParameters, @QueryParam("available") String available) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        final List<LogBookType> logbookTypes = new ArrayList<>();
        if (available == null || !Boolean.parseBoolean(available)) {
            logbookTypes.addAll(ListPager.of(deviceType.getLogBookTypes(), new LogBookTypeComparator()).from(queryParameters).find());
        } else {
            findAllAvailableLogBookTypesForDeviceType(queryParameters, deviceType, logbookTypes);
        }
        List<LogBookTypeInfo> logBookTypeInfos = LogBookTypeInfo.from(ListPager.of(logbookTypes, new LogBookTypeComparator()).find());
        return PagedInfoList.fromPagedList("logbookTypes", logBookTypeInfos, queryParameters);
    }

    private void findAllAvailableLogBookTypesForDeviceType(JsonQueryParameters queryParameters, DeviceType deviceType, List<LogBookType> logBookTypes) {
        Set<Long> deviceTypeLogBookTypeIds = asIds(deviceType.getLogBookTypes());
        for (LogBookType logBookType : this.masterDataService.findAllLogBookTypes().from(queryParameters).find()) {
            if (!deviceTypeLogBookTypeIds.contains(logBookType.getId())) {
                logBookTypes.add(logBookType);
            }
        }
    }


/*    @GET
    @Path("/{id}/logbooktypes")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.VIEW_DEVICE_TYPE)
    public Response getLogBookTypesForDeviceType(@PathParam("id") long id, @BeanParam QueryParameters queryParameters, @QueryParam("available") String available) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        List<LogBookType> resultLogBookTypes = deviceType.getLogBookTypes();
        if (available != null && Boolean.parseBoolean(available)) {
            resultLogBookTypes = findAllAvailableLogBookTypesForDeviceType(resultLogBookTypes);
        }
        return Response.ok(PagedInfoList.asJson("data",
                LogBookTypeInfo.from(ListPager.of(resultLogBookTypes, new LogBookTypeComparator()).find()), queryParameters)).build();
    }*/

   /* private List<LogBookType> findAllAvailableLogBookTypesForDeviceType(List<LogBookType> registeredLogBookTypes) {
        List<LogBookType> allLogBookTypes = masterDataService.findAllLogBookTypes().find();
        Set<Long> registeredLogBookTypeIds = new HashSet<>(registeredLogBookTypes.size());
        for (LogBookType logBookType : registeredLogBookTypes) {
            registeredLogBookTypeIds.add(logBookType.getId());
        }
        Iterator<LogBookType> logBookTypeIterator = allLogBookTypes.iterator();
        while (logBookTypeIterator.hasNext()) {
            LogBookType logBookType = logBookTypeIterator.next();
            if (registeredLogBookTypeIds.contains(logBookType.getId())) {
                logBookTypeIterator.remove();
            }
        }
        return allLogBookTypes;
    }*/

    @POST
    @Path("/{id}/logbooktypes")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_TYPE)
    public Response addLogBookTypesForDeviceType(@PathParam("id") long id, List<Long> ids) {
        if (ids.isEmpty()) {
            throw new TranslatableApplicationException(thesaurus, MessageSeeds.NO_LOGBOOK_TYPE_ID_FOR_ADDING);
        }
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        List<LogBookType> logBookTypes = new ArrayList<>(ids.size());
        for (Long logBookTypeId : ids) {
            Optional<LogBookType> logBookTypeRef = masterDataService.findLogBookType(logBookTypeId);
            if (logBookTypeRef.isPresent()) {
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
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_TYPE)
    public Response deleteLogbookTypeFromDeviceType(@PathParam("id") long id, @PathParam("lbid") long lbid) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        Optional<LogBookType> logBookTypeRef = masterDataService.findLogBookType(lbid);
        if (!logBookTypeRef.isPresent()) {
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
                if (logBookSpec.getLogBookType().getId() == logBookType.getId()) {
                    deviceConfiguration.removeLogBookSpec(logBookSpec);
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
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_TYPE, Privileges.VIEW_DEVICE_TYPE})
    public PagedInfoList getRegisterTypesForDeviceType(@PathParam("id") long id, @BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter availableFilter) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        Boolean available = availableFilter.getBoolean("available");
        List<RegisterType> matchingRegisterTypes = new ArrayList<>();
        if (available == null || !available) {
            matchingRegisterTypes.addAll(deviceType.getRegisterTypes());
        } else {
            Long deviceConfigurationId = availableFilter.getLong("deviceconfigurationid");
            if (deviceConfigurationId != null) {
                matchingRegisterTypes.addAll(findAllAvailableRegisterTypesForDeviceConfiguration(deviceType, deviceConfigurationId));
            } else {
                matchingRegisterTypes.addAll(findAllAvailableRegisterTypesForDeviceType(deviceType));
            }
        }
        List<RegisterType> registerTypes = ListPager.of(matchingRegisterTypes, new RegisterTypeComparator()).from(queryParameters).find();
        List<RegisterTypeInfo> registerTypeInfos = asInfoList(deviceType, registerTypes);
        return PagedInfoList.fromPagedList("registerTypes", registerTypeInfos, queryParameters);
    }

    private List<RegisterType> findAllAvailableRegisterTypesForDeviceType(DeviceType deviceType) {
        List<RegisterType> registerTypes = new ArrayList<>();
        Set<Long> deviceTypeRegisterTypeIds = asIds(deviceType.getRegisterTypes());
        for (RegisterType registerType : this.masterDataService.findAllRegisterTypes().find()) {
            if (!deviceTypeRegisterTypeIds.contains(registerType.getId())) {
                registerTypes.add(registerType);
            }
        }
        return registerTypes;
    }

    private List<RegisterType> findAllAvailableRegisterTypesForDeviceConfiguration(DeviceType deviceType, long deviceConfigurationId) {
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        Set<Long> unavailableRegisterTypeIds = deviceConfiguration.getRegisterSpecs().stream().map(rs -> rs.getRegisterType().getId()).collect(toSet());
        return deviceType.getRegisterTypes().stream().filter(rt->!unavailableRegisterTypeIds.contains(rt.getId())).collect(toList());
    }

    @POST
    @Path("/{id}/registertypes/{rmId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_TYPE)
    public List<RegisterTypeInfo> linkRegisterTypesToDeviceType(@PathParam("id") long id, @PathParam("rmId") long rmId) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);

        linkRegisterTypeToDeviceType(deviceType, rmId);

        deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);

        return asInfoList(deviceType, deviceType.getRegisterTypes());
    }

    @DELETE
    @Path("/{id}/registertypes/{rmId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_TYPE)
    public Response unlinkRegisterTypesFromDeviceType(@PathParam("id") long id, @PathParam("rmId") long registerTypeId) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        if (getRegisterTypeById(deviceType.getRegisterTypes(), registerTypeId) == null) {
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