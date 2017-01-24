package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.OutOfTheBoxCategory;
import com.elster.jupiter.calendar.Status;
import com.elster.jupiter.calendar.rest.CalendarInfo;
import com.elster.jupiter.calendar.rest.CalendarInfoFactory;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.RestValidationBuilder;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.AllowedCalendar;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceMessageFile;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.DeviceTypePurpose;
import com.energyict.mdc.device.config.IncompatibleDeviceLifeCycleChangeException;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.TimeOfUseOptions;
import com.energyict.mdc.device.config.exceptions.DeviceMessageFileTooBigException;
import com.energyict.mdc.device.config.security.Privileges;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.rest.info.DeviceLifeCycleInfo;
import com.energyict.mdc.device.lifecycle.config.rest.info.DeviceLifeCycleStateInfo;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.masterdata.rest.RegisterTypeInfo;
import com.energyict.mdc.masterdata.rest.RegisterTypeInfoFactory;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.upl.messages.ProtocolSupportedCalendarOptions;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

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
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
    private final DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private final Provider<DeviceConfigurationResource> deviceConfigurationResourceProvider;
    private final Provider<DeviceConfigConflictMappingResource> deviceConflictMappingResourceProvider;
    private final Provider<LoadProfileTypeResource> loadProfileTypeResourceProvider;
    private final ProtocolPluggableService protocolPluggableService;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final CalendarInfoFactory calendarInfoFactory;
    private final CalendarService calendarService;
    private final ExceptionFactory exceptionFactory;
    private final Thesaurus thesaurus;
    private final RegisterTypeOnDeviceTypeInfoFactory registerTypeOnDeviceTypeInfoFactory;
    private final RegisterTypeInfoFactory registerTypeInfoFactory;

    @Inject
    public DeviceTypeResource(
            ResourceHelper resourceHelper,
            MasterDataService masterDataService,
            DeviceConfigurationService deviceConfigurationService,
            DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService, Provider<DeviceConfigConflictMappingResource> deviceConflictMappingResourceProvider,
            ProtocolPluggableService protocolPluggableService,
            Provider<DeviceConfigurationResource> deviceConfigurationResourceProvider,
            Provider<LoadProfileTypeResource> loadProfileTypeResourceProvider,
            ConcurrentModificationExceptionFactory conflictFactory, CalendarInfoFactory calendarInfoFactory,
            CalendarService calendarService,
            ExceptionFactory exceptionFactory,
            Thesaurus thesaurus,
            RegisterTypeOnDeviceTypeInfoFactory registerTypeOnDeviceTypeInfoFactory, RegisterTypeInfoFactory registerTypeInfoFactory) {
        this.resourceHelper = resourceHelper;
        this.masterDataService = masterDataService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
        this.protocolPluggableService = protocolPluggableService;
        this.loadProfileTypeResourceProvider = loadProfileTypeResourceProvider;
        this.deviceConfigurationResourceProvider = deviceConfigurationResourceProvider;
        this.deviceConflictMappingResourceProvider = deviceConflictMappingResourceProvider;
        this.conflictFactory = conflictFactory;
        this.calendarInfoFactory = calendarInfoFactory;
        this.calendarService = calendarService;
        this.exceptionFactory = exceptionFactory;
        this.thesaurus = thesaurus;
        this.registerTypeOnDeviceTypeInfoFactory = registerTypeOnDeviceTypeInfoFactory;
        this.registerTypeInfoFactory = registerTypeInfoFactory;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public PagedInfoList getAllDeviceTypes(@BeanParam JsonQueryParameters queryParameters) {
        Finder<DeviceType> deviceTypeFinder = deviceConfigurationService.findAllDeviceTypes();
        List<DeviceType> allDeviceTypes = deviceTypeFinder.from(queryParameters).find();
        List<DeviceTypeInfo> deviceTypeInfos = DeviceTypeInfo.from(allDeviceTypes);
        return PagedInfoList.fromPagedList("deviceTypes", deviceTypeInfos, queryParameters);
    }

    @DELETE
    @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response deleteDeviceType(@PathParam("id") long id, DeviceTypeInfo info) {
        info.id = id;
        resourceHelper.lockDeviceTypeOrThrowException(info).delete();
        return Response.ok().build();

    }

    @POST
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public DeviceTypeInfo createDeviceType(DeviceTypeInfo deviceTypeInfo) {
        Optional<DeviceProtocolPluggableClass> deviceProtocolPluggableClass = protocolPluggableService.findDeviceProtocolPluggableClassByName(deviceTypeInfo.deviceProtocolPluggableClassName);
        Optional<DeviceLifeCycle> deviceLifeCycleRef = deviceTypeInfo.deviceLifeCycleId != null ? resourceHelper.findDeviceLifeCycleById(deviceTypeInfo.deviceLifeCycleId) : Optional
                .empty();
        DeviceType deviceType;
        if (deviceTypeInfo.deviceTypePurpose.equals(DeviceTypePurposeTranslationKeys.DATALOGGER_SLAVE.getKey())) {
            deviceType = deviceConfigurationService.newDataloggerSlaveDeviceTypeBuilder(deviceTypeInfo.name, deviceLifeCycleRef
                    .isPresent() ? deviceLifeCycleRef.get() : null).create();
        } else {
            deviceType = deviceConfigurationService.newDeviceTypeBuilder(deviceTypeInfo.name,
                    deviceProtocolPluggableClass.isPresent() ? deviceProtocolPluggableClass.get() : null,
                    deviceLifeCycleRef.isPresent() ? deviceLifeCycleRef.get() : null).create();
        }
        return DeviceTypeInfo.from(deviceType);
    }

    @PUT
    @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response updateDeviceType(@PathParam("id") long id, DeviceTypeInfo deviceTypeInfo) {
        deviceTypeInfo.id = id;
        DeviceType deviceType = resourceHelper.lockDeviceTypeOrThrowException(deviceTypeInfo);
        deviceType.setName(deviceTypeInfo.name);
        deviceType.setDeviceTypePurpose(getDeviceTypePurpose(deviceTypeInfo));
        if (!deviceType.isDataloggerSlave()) {
            deviceType.setDeviceProtocolPluggableClass(deviceTypeInfo.deviceProtocolPluggableClassName);
        }
        if (deviceTypeInfo.registerTypes != null) {
            updateRegisterTypeAssociations(deviceType, deviceTypeInfo.registerTypes);
        }
        if (deviceTypeInfo.deviceLifeCycleId != null && (deviceType.getConfigurations().isEmpty() ||
                deviceType.getConfigurations().stream().noneMatch(DeviceConfiguration::isActive))) {
            DeviceLifeCycle targetDeviceLifeCycle = resourceHelper.findDeviceLifeCycleByIdOrThrowException(deviceTypeInfo.deviceLifeCycleId);
            try {
                deviceConfigurationService.changeDeviceLifeCycle(deviceType, targetDeviceLifeCycle);
            } catch (IncompatibleDeviceLifeCycleChangeException mappingEx) {
                DeviceLifeCycle oldDeviceLifeCycle = deviceType.getDeviceLifeCycle();
                ChangeDeviceLifeCycleInfo info = getChangeDeviceLifeCycleFailInfo(mappingEx, oldDeviceLifeCycle, targetDeviceLifeCycle);
                return Response.status(Response.Status.BAD_REQUEST).entity(info).build();
            }
        }
        if(deviceTypeInfo.fileManagementEnabled) {
            deviceType.enableFileManagement();
        } else {
            deviceType.disableFileManagement();
        }
        deviceType.update();
        return Response.ok(DeviceTypeInfo.from(deviceType)).build();
    }

    private DeviceTypePurpose getDeviceTypePurpose(DeviceTypeInfo deviceTypeInfo) {
        return Arrays.stream(DeviceTypePurpose.values())
                .filter(candidate -> candidate.name().equals(deviceTypeInfo.deviceTypePurpose))
                .findFirst()
                .orElse(null);
    }

    private ChangeDeviceLifeCycleInfo getChangeDeviceLifeCycleFailInfo(IncompatibleDeviceLifeCycleChangeException lifeCycleChangeError, DeviceLifeCycle currentDeviceLifeCycle, DeviceLifeCycle targetDeviceLifeCycle) {
        ChangeDeviceLifeCycleInfo info = new ChangeDeviceLifeCycleInfo();
        info.success = false;
        info.errorMessage = thesaurus.getFormat(MessageSeeds.UNABLE_TO_CHANGE_DEVICE_LIFE_CYCLE)
                .format(targetDeviceLifeCycle.getName());
        info.currentDeviceLifeCycle = new DeviceLifeCycleInfo(currentDeviceLifeCycle);
        info.targetDeviceLifeCycle = new DeviceLifeCycleInfo(targetDeviceLifeCycle);
        info.notMappableStates = lifeCycleChangeError.getMissingStates()
                .stream()
                .map(state -> new DeviceLifeCycleStateInfo(deviceLifeCycleConfigurationService, null, state))
                .collect(Collectors.toList());
        return info;
    }

    @GET
    @Transactional
    @Path("/{id}/capabilities")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE_TYPE})
    public Response getDeviceTypeCapabilites(@PathParam("id") long id, @BeanParam JsonQueryParameters queryParameters) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        List<IdWithNameInfo> capabilities =
                deviceType.getDeviceProtocolPluggableClass().isPresent() && deviceType.getDeviceProtocolPluggableClass().get().supportsFileManagement() ?
                        Collections.singletonList(new IdWithNameInfo(null, "devicetype.supports.filemanagement")) :
                        Collections.emptyList();
        return Response.ok(PagedInfoList.fromCompleteList("capabilities", capabilities, queryParameters)).build();
    }

    @PUT
    @Transactional
    @Path("/{id}/devicelifecycle")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response updateDeviceLifeCycleForDeviceType(@PathParam("id") long id, ChangeDeviceLifeCycleInfo info) {
        if (info.targetDeviceLifeCycle.id == 0) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_IS_REQUIRED, "deviceLifeCycleId");
        }
        new RestValidationBuilder()
                .isCorrectId(info.targetDeviceLifeCycle != null ? info.targetDeviceLifeCycle.id : null, "deviceLifeCycleId")
                .validate();
        DeviceType deviceType = resourceHelper.lockDeviceTypeOrThrowException(id, info.version, info.name);
        DeviceLifeCycle oldDeviceLifeCycle = deviceType.getDeviceLifeCycle();
        DeviceLifeCycle targetDeviceLifeCycle = resourceHelper.findDeviceLifeCycleByIdOrThrowException(info.targetDeviceLifeCycle.id);
        try {
            deviceConfigurationService.changeDeviceLifeCycle(deviceType, targetDeviceLifeCycle);
        } catch (IncompatibleDeviceLifeCycleChangeException mappingEx) {
            info = getChangeDeviceLifeCycleFailInfo(mappingEx, oldDeviceLifeCycle, targetDeviceLifeCycle);
            return Response.status(Response.Status.BAD_REQUEST).entity(info).build();
        }
        return Response.ok(DeviceTypeInfo.from(deviceType)).build();
    }

    @GET
    @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public DeviceTypeInfo findDeviceType(@PathParam("id") long id) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        return DeviceTypeInfo.from(deviceType, deviceType.getRegisterTypes(), registerTypeInfoFactory);
    }

    @GET
    @Transactional
    @Path("/{id}/custompropertysets")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public PagedInfoList getDeviceTypeCustomPropertySetUsage(@PathParam("id") long id, @BeanParam JsonQueryFilter filter, @BeanParam JsonQueryParameters queryParameters) {
        boolean isNotLinked = !filter.getBoolean("linked");
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        List<RegisteredCustomPropertySet> registeredCustomPropertySets;
        if (isNotLinked) {
            registeredCustomPropertySets = resourceHelper.findAllCustomPropertySetsByDomain(Device.class)
                    .stream()
                    .filter(f -> !deviceType.getCustomPropertySets()
                            .stream()
                            .map(RegisteredCustomPropertySet::getId)
                            .collect(Collectors.toList())
                            .contains(f.getId()))
                    .collect(Collectors.toList());
        } else {
            registeredCustomPropertySets = deviceType.getCustomPropertySets();
        }
        return PagedInfoList.fromPagedList("deviceTypeCustomPropertySets", DeviceTypeCustomPropertySetInfo.from(registeredCustomPropertySets), queryParameters);
    }

    @PUT
    @Transactional
    @Path("/{id}/custompropertysets")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response addDeviceTypeCustomPropertySetUsage(@PathParam("id") long id, List<DeviceTypeCustomPropertySetInfo> infos) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        infos.stream().forEach(deviceTypeCustomPropertySetInfo ->
                deviceType.addCustomPropertySet(resourceHelper.findDeviceTypeCustomPropertySetByIdOrThrowException(deviceTypeCustomPropertySetInfo.id, Device.class)));
        return Response.ok().build();
    }

    @DELETE
    @Transactional
    @Path("/{deviceTypeId}/custompropertysets/{customPropertySetId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response deleteDeviceTypeCustomPropertySetUsage(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("customPropertySetId") long customPropertySetId) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        deviceType.removeCustomPropertySet(resourceHelper.findDeviceTypeCustomPropertySetByIdOrThrowException(customPropertySetId, Device.class));
        return Response.ok().build();
    }

    @Path("/{id}/loadprofiletypes")
    public LoadProfileTypeResource getLoadProfileTypesResource() {
        return loadProfileTypeResourceProvider.get();
    }

    @GET
    @Transactional
    @Path("/{id}/logbooktypes")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public PagedInfoList getLogBookTypesForDeviceType(@PathParam("id") long id, @BeanParam JsonQueryParameters queryParameters, @QueryParam("available") String available) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        final List<LogBookType> logbookTypes = new ArrayList<>();
        if (available == null || !Boolean.parseBoolean(available)) {
            logbookTypes.addAll(ListPager.of(deviceType.getLogBookTypes(), new LogBookTypeComparator())
                    .from(queryParameters)
                    .find());
        } else {
            findAllAvailableLogBookTypesForDeviceType(queryParameters, deviceType, logbookTypes);
        }
        List<LogBookTypeInfo> logBookTypeInfos = LogBookTypeInfo.from(ListPager.of(logbookTypes, new LogBookTypeComparator())
                .find(), deviceType);
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

    @POST
    @Transactional
    @Path("/{id}/logbooktypes")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
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
        return Response.ok(LogBookTypeInfo.from(logBookTypes, deviceType)).build();
    }

    @DELETE
    @Transactional
    @Path("/{id}/logbooktypes/{lbid}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response deleteLogbookTypeFromDeviceType(@PathParam("id") long id, @PathParam("lbid") long lbid, LogBookTypeInfo info) {
        info.parent.id = id;
        LogBookType logBookType = resourceHelper.lockDeviceTypeLogBookOrThrowException(info);
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        deleteLogBookTypeFromChildConfigurations(deviceType, logBookType);
        deviceType.removeLogBookType(logBookType);
        return Response.ok().build();
    }

    private void deleteLogBookTypeFromChildConfigurations(DeviceType deviceType, LogBookType logBookType) {
        List<DeviceConfiguration> deviceConfigurations = deviceConfigurationService.findDeviceConfigurationsUsingDeviceType(deviceType)
                .find();
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

    @Path("/{deviceTypeId}/conflictmappings")
    public DeviceConfigConflictMappingResource getDeviceConflictMappingResource() {
        return deviceConflictMappingResourceProvider.get();
    }

    @GET
    @Transactional
    @Path("/{id}/registertypes")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
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
        List<RegisterType> registerTypes = ListPager.of(matchingRegisterTypes, new RegisterTypeComparator())
                .from(queryParameters)
                .find();
        List<RegisterTypeOnDeviceTypeInfo> registerTypeInfos = asInfoList(deviceType, registerTypes);
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
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationByIdOrThrowException(deviceConfigurationId);
        Set<Long> unavailableRegisterTypeIds = deviceConfiguration.getRegisterSpecs()
                .stream()
                .map(rs -> rs.getRegisterType().getId())
                .collect(toSet());
        return deviceType.getRegisterTypes()
                .stream()
                .filter(rt -> !unavailableRegisterTypeIds.contains(rt.getId()))
                .collect(toList());
    }

    @GET
    @Transactional
    @Path("/{id}/registertypes/{registerTypeId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public RegisterTypeOnDeviceTypeInfo getRegisterForDeviceType(@PathParam("id") long deviceTypeId,
                                                                 @PathParam("registerTypeId") long registerTypeId) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        RegisterType registerType = resourceHelper.findRegisterTypeByIdOrThrowException(registerTypeId);
        return registerTypeOnDeviceTypeInfoFactory.asInfo(registerType,
                false, false, false,
                deviceType.getRegisterTypeTypeCustomPropertySet(registerType),
                masterDataService.getOrCreatePossibleMultiplyReadingTypesFor(registerType.getReadingType()),
                registerType.getReadingType().getCalculatedReadingType().orElse(registerType.getReadingType()));
    }

    @PUT
    @Transactional
    @Path("/{id}/registertypes/{registerTypeId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response changeRegisterTypeOnDeviceTypeCustomPropertySet(@PathParam("id") long deviceTypeId,
                                                                    @PathParam("registerTypeId") long registerTypeId,
                                                                    RegisterTypeOnDeviceTypeInfo registerTypeOnDeviceTypeInfo) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        RegisterType registerType = resourceHelper.findRegisterTypeByIdOrThrowException(registerTypeId);
        deviceType.addRegisterTypeCustomPropertySet(registerType, registerTypeOnDeviceTypeInfo.customPropertySet.id > 0 ?
                resourceHelper.findDeviceTypeCustomPropertySetByIdOrThrowException(registerTypeOnDeviceTypeInfo.customPropertySet.id, RegisterSpec.class) : null);
        return Response.ok().build();
    }

    @GET
    @Transactional
    @Path("/{id}/registertypes/custompropertysets")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public Response getRegisterCustomPropertySets() {
        return Response.ok(DeviceTypeCustomPropertySetInfo.from(resourceHelper.findAllCustomPropertySetsByDomain(RegisterSpec.class)))
                .build();
    }

    @POST
    @Transactional
    @Path("/{id}/registertypes/{rmId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public List<RegisterTypeOnDeviceTypeInfo> linkRegisterTypesToDeviceType(@PathParam("id") long id, @PathParam("rmId") long rmId) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        linkRegisterTypeToDeviceType(deviceType, rmId);
        DeviceType reloaded = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        return asInfoList(reloaded, reloaded.getRegisterTypes());
    }

    @DELETE
    @Transactional
    @Path("/{id}/registertypes/{rmId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response unlinkRegisterTypesFromDeviceType(@PathParam("id") long id, @PathParam("rmId") long registerTypeId, RegisterTypeInfo info) {
        RegisterType registerType = resourceHelper.lockDeviceTypeRegisterTypeOrThrowException(info);
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        deviceType.removeRegisterType(registerType);
        return Response.ok().build();
    }

    @GET
    @Path("/{id}/files")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.VIEW_DEVICE_TYPE)
    public Response getFiles(@PathParam("id") long id) {
        List<DeviceMessageFileInfo> files = resourceHelper.findDeviceTypeByIdOrThrowException(id)
                .getDeviceMessageFiles()
                .stream()
                .sorted(Comparator.comparing(DeviceMessageFile::getName))
                .map(DeviceMessageFileInfo::new)
                .collect(Collectors.toList());

        return Response.ok(files).build();
    }

    @DELETE
    @Path("/{id}/files/{fileId}")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response deleteFile(@PathParam("id") long id, @PathParam("fileId") long fileId, DeviceMessageFileInfo info) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        DeviceMessageFile file = deviceType.getDeviceMessageFiles()
                .stream()
                .filter(deviceMessageFile -> deviceMessageFile.getId() == fileId)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
        deviceType.removeDeviceMessageFile(file);
        return Response.ok().build();
    }

    @POST
    @Transactional
    @Path("/{id}/files/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({MediaType.TEXT_PLAIN})
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response uploadFile(@PathParam("id") long deviceTypeId, @FormDataParam("uploadField") InputStream fileInputStream,
                                  @FormDataParam("uploadField") FormDataContentDisposition contentDispositionHeader,
                               @FormDataParam("fileName") String fileName) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        addFileToDeviceType(deviceType, fileInputStream, fileName);

        return Response.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN).build();
    }

    @GET
    @Path("/{id}/timeofuse")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE_TYPE, Privileges.Constants.ADMINISTRATE_DEVICE_TYPE})
    public Response getCalendars(@PathParam("id") long id) {
        List<AllowedCalendarInfo> infos;
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        infos = deviceType.getAllowedCalendars()
                .stream()
                .map(this::getAllowedCalendarInfo)
                .collect(Collectors.toList());

        return Response.ok(infos).build();
    }

    @DELETE
    @Transactional
    @Path("/{id}/timeofuse/{calendarId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response removeCalendar(@PathParam("id") long id, @PathParam("calendarId") long calendarId, AllowedCalendarInfo allowedCalendarInfo) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        Optional<AllowedCalendar> allowedCalendar =
                deviceType
                        .getAllowedCalendars()
                        .stream()
                        .filter(each -> each.getId() == allowedCalendarInfo.id)
                        .findFirst();
        if (allowedCalendar.isPresent()) {
            deviceType.removeCalendar(allowedCalendar.get());
            return Response.ok().build();
        } else {
            throw new WebApplicationException("Calendar with id " + allowedCalendarInfo.id + " not found on device type with id " + id, Response.Status.NOT_FOUND);
        }
    }


    @GET
    @Path("/{id}/timeofuse/{calendarId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE_TYPE, Privileges.Constants.ADMINISTRATE_DEVICE_TYPE})
    public Response getCalendar(@PathParam("id") long id, @PathParam("calendarId") long calendarId, @QueryParam("weekOf") long milliseconds) {
        if (milliseconds <= 0) {
            return Response.ok(calendarService.findCalendar(calendarId)
                    .map(calendarInfoFactory::detailedFromCalendar)
                    .orElseThrow(IllegalArgumentException::new)).build();
        } else {
            Instant instant = Instant.ofEpochMilli(milliseconds);
            Calendar calendar = calendarService.findCalendar(calendarId).get();
            LocalDate localDate = LocalDateTime.ofInstant(instant, ZoneId.of("UTC"))
                    .toLocalDate();

            return Response.ok(transformToWeekCalendar(calendar, localDate)).build();
        }
    }

    @GET
    @Path("/{id}/unusedcalendars")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response getUnusedCalendars(@PathParam("id") long id) {
        Set<Calendar> usedCalendars;
        List<CalendarInfo> infos;
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        usedCalendars = deviceType.getAllowedCalendars()
                .stream()
                .filter(ac -> !ac.isGhost())
                .map(allowedCalendar -> allowedCalendar.getCalendar().get())
                .collect(Collectors.toSet());

        infos = calendarService.findAllCalendars()
                .stream()
                .filter(calendar -> !usedCalendars.contains(calendar)
                        && calendar.getStatus().equals(Status.ACTIVE)
                        && calendar.getCategory().getId()==(calendarService.findCategoryByName(OutOfTheBoxCategory.TOU.getDefaultDisplayName()).get().getId()))
                .map(calendarInfoFactory::summaryFromCalendar)
                .collect(Collectors.toList());

        return Response.ok(infos).build();
    }

    @PUT
    @Transactional
    @Path("/{id}/unusedcalendars")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response addCalendar(@PathParam("id") long id, List<CalendarInfo> calendarInfos) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        calendarInfos.stream()
                .forEach(info -> deviceType.addCalendar(calendarService.findCalendar(info.id).get()));

        return Response.ok().build();
    }

    @GET
    @Transactional
    @Path("/{id}/timeofuseoptions/{dummyid}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE_TYPE, Privileges.Constants.ADMINISTRATE_DEVICE_TYPE})
    public Response getTimeOfUseManagementOptions(@PathParam("id") long id) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        TimeOfUseOptionsInfo timeOfUseOptionsInfo = getTimeOfUseOptions(deviceType);

        return Response.ok(timeOfUseOptionsInfo).build();
    }

    @PUT
    @Transactional
    @Path("/{id}/timeofuseoptions/{dummyid}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response changeTimeOfUseOptions(@PathParam("id") long id, @PathParam("dummyid") long dummyID, TimeOfUseOptionsInfo info) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        Optional<TimeOfUseOptions> timeOfUseOptions = resourceHelper.findAndLockTimeOfUseOptionsByIdAndVersion(deviceType, info.version);
        if (!timeOfUseOptions.isPresent() && resourceHelper.findTimeOfUseOptionsId(deviceType) != 0) {
            throw conflictFactory.contextDependentConflictOn(deviceType.getName())
                    .withActualVersion(() -> resourceHelper.findTimeOfUseOptionsId(deviceType))
                    .build();
        }

        if (info.isAllowed && info.allowedOptions != null){
            Set<ProtocolSupportedCalendarOptions> supportedCalendarOptions = deviceConfigurationService.getSupportedTimeOfUseOptionsFor(deviceType, false);
            Set<ProtocolSupportedCalendarOptions> newAllowedOptions = info.allowedOptions.stream()
                    .map(allowedOption -> ProtocolSupportedCalendarOptions.from((String) allowedOption.id))
                    .flatMap(Functions.asStream())
                    .filter(supportedCalendarOptions::contains)
                    .collect(Collectors.toSet());
            TimeOfUseOptions options = timeOfUseOptions.orElseGet(() -> deviceConfigurationService.newTimeOfUseOptions(deviceType));
            options.setOptions(newAllowedOptions);
            options.save();
        } else {
            timeOfUseOptions.ifPresent(TimeOfUseOptions::delete);
        }
        return Response.ok(getTimeOfUseOptions(deviceType)).build();
    }

    private TimeOfUseOptionsInfo getTimeOfUseOptions(DeviceType deviceType) {
        TimeOfUseOptionsInfo timeOfUseOptionsInfo = new TimeOfUseOptionsInfo();
        Set<ProtocolSupportedCalendarOptions> supportedCalendarOptions = deviceConfigurationService.getSupportedTimeOfUseOptionsFor(deviceType, false);
        Optional<TimeOfUseOptions> timeOfUseOptions = deviceConfigurationService.findTimeOfUseOptions(deviceType);
        Set<ProtocolSupportedCalendarOptions> allowedOptions = timeOfUseOptions.map(TimeOfUseOptions::getOptions)
                .orElse(Collections.emptySet());

        supportedCalendarOptions
                .forEach(op -> timeOfUseOptionsInfo.supportedOptions.add(new OptionInfo(op.getId(), thesaurus.getString(op.getId(), op.getId()))));
        allowedOptions
                .forEach(op ->
                        timeOfUseOptionsInfo.allowedOptions.add(new OptionInfo(op.getId(), thesaurus.getString(op.getId(), op.getId()))));

        timeOfUseOptionsInfo.isAllowed = !allowedOptions.isEmpty();
        timeOfUseOptionsInfo.version = timeOfUseOptions.map(TimeOfUseOptions::getVersion).orElse(0L);

        return timeOfUseOptionsInfo;

    }

    private CalendarInfo transformToWeekCalendar(Calendar calendar, LocalDate localDate) {
        return calendarInfoFactory.detailedWeekFromCalendar(calendar, localDate);
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

    private List<RegisterTypeOnDeviceTypeInfo> asInfoList(DeviceType deviceType, List<RegisterType> registerTypes) {
        List<RegisterTypeOnDeviceTypeInfo> registerTypeInfos = new ArrayList<>();
        for (RegisterType registerType : registerTypes) {
            boolean isLinkedByDeviceType = !deviceConfigurationService.findDeviceTypesUsingRegisterType(registerType)
                    .isEmpty();
            boolean isLinkedByActiveRegisterSpec = !deviceConfigurationService.findActiveRegisterSpecsByDeviceTypeAndRegisterType(deviceType, registerType)
                    .isEmpty();
            boolean isLinkedByInactiveRegisterSpec = !deviceConfigurationService.findInactiveRegisterSpecsByDeviceTypeAndRegisterType(deviceType, registerType)
                    .isEmpty();
            RegisterTypeOnDeviceTypeInfo info = registerTypeOnDeviceTypeInfoFactory.asInfo(
                    registerType,
                    isLinkedByDeviceType,
                    isLinkedByActiveRegisterSpec,
                    isLinkedByInactiveRegisterSpec,
                    deviceType.getRegisterTypeTypeCustomPropertySet(registerType),
                    masterDataService.getOrCreatePossibleMultiplyReadingTypesFor(registerType.getReadingType()),
                    registerType.getReadingType());
            if (isLinkedByDeviceType) {
                info.parent = new VersionInfo<>(deviceType.getId(), deviceType.getVersion());
            }
            registerTypeInfos.add(info);
        }
        return registerTypeInfos;
    }

    private AllowedCalendarInfo getAllowedCalendarInfo(AllowedCalendar allowedCalendar) {
        if (allowedCalendar.isGhost()) {
            return new AllowedCalendarInfo(allowedCalendar);
        } else {
            return new AllowedCalendarInfo(allowedCalendar, calendarInfoFactory.summaryFromCalendar(allowedCalendar.getCalendar().get()));
        }
    }

    private void addFileToDeviceType(DeviceType deviceType, InputStream inputStream, String fileName) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream(); InputStream fis = inputStream) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) != -1) {
                out.write(buffer, 0, length);
                if (out.size() > DeviceConfigurationService.MAX_DEVICE_MESSAGE_FILE_SIZE_BYTES) {
                    throw new DeviceMessageFileTooBigException(DeviceConfigurationService.MAX_DEVICE_MESSAGE_FILE_SIZE_MB, thesaurus);
                }
            }
            byte[] firmwareFile = out.toByteArray();
            deviceType.addDeviceMessageFile(new ByteArrayInputStream(firmwareFile), fileName);
        } catch (IOException ex) {
            throw exceptionFactory.newException(MessageSeeds.FILE_IO);
        }
    }
}
