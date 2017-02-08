/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.rest.CalendarInfo;
import com.elster.jupiter.calendar.rest.CalendarInfoFactory;
import com.elster.jupiter.cps.ValuesRangeConflictType;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallFilter;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.rest.ServiceCallInfo;
import com.elster.jupiter.servicecall.rest.ServiceCallInfoFactory;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.rest.IntervalInfo;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.AllowedCalendar;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceTypePurpose;
import com.energyict.mdc.device.config.GatewayType;
import com.energyict.mdc.device.config.TimeOfUseOptions;
import com.energyict.mdc.device.data.ActiveEffectiveCalendar;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.DevicesForConfigChangeSearch;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.exceptions.CannotChangeDeviceConfigStillUnresolvedConflicts;
import com.energyict.mdc.device.data.exceptions.NoStatusInformationTaskException;
import com.energyict.mdc.device.data.rest.DevicePrivileges;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.TopologyTimeline;
import com.energyict.mdc.protocol.api.calendars.ProtocolSupportedCalendarOptions;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.Checks.is;
import static com.energyict.mdc.protocol.api.messaging.DeviceMessageId.ACTIVITY_CALENDAR_SPECIAL_DAY_CALENDAR_SEND;
import static com.energyict.mdc.protocol.api.messaging.DeviceMessageId.ACTIVITY_CALENDAR_SPECIAL_DAY_CALENDAR_SEND_WITH_TYPE;
import static com.energyict.mdc.protocol.api.messaging.DeviceMessageId.ACTIVITY_CALENDER_SEND;
import static com.energyict.mdc.protocol.api.messaging.DeviceMessageId.ACTIVITY_CALENDER_SEND_WITH_DATE;
import static com.energyict.mdc.protocol.api.messaging.DeviceMessageId.ACTIVITY_CALENDER_SEND_WITH_DATETIME;
import static com.energyict.mdc.protocol.api.messaging.DeviceMessageId.ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_CONTRACT;
import static com.energyict.mdc.protocol.api.messaging.DeviceMessageId.ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_TYPE;
import static com.energyict.mdc.protocol.api.messaging.DeviceMessageId.ACTIVITY_CALENDER_SPECIAL_DAY_CALENDAR_SEND_WITH_CONTRACT_AND_DATETIME;

@Path("/devices")
public class DeviceResource {
    private static final int RECENTLY_ADDED_COUNT = 5;

    private final DeviceService deviceService;
    private final TopologyService topologyService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;
    private final Provider<ProtocolDialectResource> protocolDialectResourceProvider;
    private final Provider<LoadProfileResource> loadProfileResourceProvider;
    private final Provider<LogBookResource> logBookResourceProvider;
    private final Provider<DeviceValidationResource> deviceValidationResourceProvider;
    private final Provider<DeviceEstimationResource> deviceEstimationResourceProvider;
    private final Provider<RegisterResource> registerResourceProvider;
    private final Provider<BulkScheduleResource> bulkScheduleResourceProvider;
    private final Provider<DeviceScheduleResource> deviceScheduleResourceProvider;
    private final Provider<DeviceComTaskResource> deviceComTaskResourceProvider;
    private final Provider<SecurityPropertySetResource> securityPropertySetResourceProvider;
    private final Provider<ConnectionMethodResource> connectionMethodResourceProvider;
    private final Provider<DeviceMessageResource> deviceCommandResourceProvider;
    private final Provider<DeviceLabelResource> deviceLabelResourceProvider;
    private final Provider<ConnectionResource> connectionResourceProvider;
    private final Provider<ChannelResource> channelsOnDeviceResourceProvider;
    private final DeviceMessageSpecificationService deviceMessageSpecificationService;
    private final DeviceMessageSpecInfoFactory deviceMessageSpecInfoFactory;
    private final DeviceMessageCategoryInfoFactory deviceMessageCategoryInfoFactory;
    private final Provider<DeviceProtocolPropertyResource> devicePropertyResourceProvider;
    private final Provider<DeviceHistoryResource> deviceHistoryResourceProvider;
    private final Provider<DeviceLifeCycleActionResource> deviceLifeCycleActionResourceProvider;
    private final Provider<GoingOnResource> goingOnResourceProvider;
    private final DeviceInfoFactory deviceInfoFactory;
    private final DeviceAttributesInfoFactory deviceAttributesInfoFactory;
    private final LocationInfoFactory locationInfoFactory;
    private final DevicesForConfigChangeSearchFactory devicesForConfigChangeSearchFactory;
    private final ServiceCallInfoFactory serviceCallInfoFactory;
    private final TransactionService transactionService;
    private final ServiceCallService serviceCallService;
    private final CalendarInfoFactory calendarInfoFactory;
    private final TimeOfUseInfoFactory timeOfUseInfoFactory;
    private final CalendarService calendarService;
    private final DeviceMessageService deviceMessageService;
    private final Clock clock;
    private final DataLoggerSlaveDeviceInfoFactory dataLoggerSlaveDeviceInfoFactory;

    @Inject
    public DeviceResource(
            DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService, ResourceHelper resourceHelper,
            ExceptionFactory exceptionFactory,
            DeviceService deviceService,
            TopologyService topologyService,
            DeviceConfigurationService deviceConfigurationService,
            Provider<ProtocolDialectResource> protocolDialectResourceProvider,
            Provider<LoadProfileResource> loadProfileResourceProvider,
            Provider<LogBookResource> logBookResourceProvider,
            Provider<RegisterResource> registerResourceProvider,
            Provider<DeviceValidationResource> deviceValidationResourceProvider,
            Provider<DeviceEstimationResource> deviceEstimationResourceProvider,
            Provider<BulkScheduleResource> bulkScheduleResourceProvider,
            Provider<DeviceScheduleResource> deviceScheduleResourceProvider,
            Provider<DeviceComTaskResource> deviceComTaskResourceProvider,
            Provider<DeviceMessageResource> deviceCommandResourceProvider,
            Provider<ConnectionResource> connectionResourceProvider,
            DeviceMessageSpecificationService deviceMessageSpecificationService,
            DeviceMessageSpecInfoFactory deviceMessageSpecInfoFactory,
            DeviceMessageCategoryInfoFactory deviceMessageCategoryInfoFactory,
            Provider<SecurityPropertySetResource> securityPropertySetResourceProvider,
            Provider<DeviceLabelResource> deviceLabelResourceProvider,
            Provider<ConnectionMethodResource> connectionMethodResourceProvider,
            Provider<ChannelResource> channelsOnDeviceResourceProvider,
            Provider<DeviceProtocolPropertyResource> devicePropertyResourceProvider,
            Provider<DeviceHistoryResource> deviceHistoryResourceProvider,
            Provider<DeviceLifeCycleActionResource> deviceLifeCycleActionResourceProvider,
            Provider<GoingOnResource> goingOnResourceProvider,
            DeviceInfoFactory deviceInfoFactory,
            DeviceAttributesInfoFactory deviceAttributesInfoFactory,
            LocationInfoFactory locationInfoFactory,
            DevicesForConfigChangeSearchFactory devicesForConfigChangeSearchFactory,
            ServiceCallInfoFactory serviceCallInfoFactory,
            TransactionService transactionService,
            ServiceCallService serviceCallService,
            CalendarInfoFactory calendarInfoFactory,
            TimeOfUseInfoFactory timeOfUseInfoFactory,
            CalendarService calendarService,
            DeviceMessageService deviceMessageService,
            Clock clock,
            DataLoggerSlaveDeviceInfoFactory dataLoggerSlaveDeviceInfoFactory) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.deviceService = deviceService;
        this.topologyService = topologyService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.protocolDialectResourceProvider = protocolDialectResourceProvider;
        this.loadProfileResourceProvider = loadProfileResourceProvider;
        this.logBookResourceProvider = logBookResourceProvider;
        this.registerResourceProvider = registerResourceProvider;
        this.deviceValidationResourceProvider = deviceValidationResourceProvider;
        this.deviceEstimationResourceProvider = deviceEstimationResourceProvider;
        this.bulkScheduleResourceProvider = bulkScheduleResourceProvider;
        this.deviceScheduleResourceProvider = deviceScheduleResourceProvider;
        this.deviceComTaskResourceProvider = deviceComTaskResourceProvider;
        this.securityPropertySetResourceProvider = securityPropertySetResourceProvider;
        this.connectionMethodResourceProvider = connectionMethodResourceProvider;
        this.deviceCommandResourceProvider = deviceCommandResourceProvider;
        this.deviceLabelResourceProvider = deviceLabelResourceProvider;
        this.connectionResourceProvider = connectionResourceProvider;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        this.deviceMessageSpecInfoFactory = deviceMessageSpecInfoFactory;
        this.deviceMessageCategoryInfoFactory = deviceMessageCategoryInfoFactory;
        this.channelsOnDeviceResourceProvider = channelsOnDeviceResourceProvider;
        this.devicePropertyResourceProvider = devicePropertyResourceProvider;
        this.deviceHistoryResourceProvider = deviceHistoryResourceProvider;
        this.deviceLifeCycleActionResourceProvider = deviceLifeCycleActionResourceProvider;
        this.goingOnResourceProvider = goingOnResourceProvider;
        this.deviceInfoFactory = deviceInfoFactory;
        this.deviceAttributesInfoFactory = deviceAttributesInfoFactory;
        this.locationInfoFactory = locationInfoFactory;
        this.devicesForConfigChangeSearchFactory = devicesForConfigChangeSearchFactory;
        this.serviceCallInfoFactory = serviceCallInfoFactory;
        this.transactionService = transactionService;
        this.serviceCallService = serviceCallService;
        this.calendarInfoFactory = calendarInfoFactory;
        this.timeOfUseInfoFactory = timeOfUseInfoFactory;
        this.calendarService = calendarService;
        this.deviceMessageService = deviceMessageService;
        this.clock = clock;
        this.dataLoggerSlaveDeviceInfoFactory = dataLoggerSlaveDeviceInfoFactory;
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public PagedInfoList getAllDevices(@BeanParam JsonQueryParameters queryParameters, @BeanParam StandardParametersBean params, @Context UriInfo uriInfo) {
        Condition condition;
        MultivaluedMap<String, String> uriParams = uriInfo.getQueryParameters();
        if (uriParams.containsKey("filter")) {
            condition = resourceHelper.getQueryConditionForDevice(uriInfo.getQueryParameters());
        } else {
            condition = resourceHelper.getQueryConditionForDevice(params);
        }
        Finder<Device> allDevicesFinder = deviceService.findAllDevices(condition);
        List<Device> allDevices = allDevicesFinder.from(queryParameters).find();
        List<DeviceInfo> deviceInfos = deviceInfoFactory.fromDevices(allDevices); //DeviceInfo.from(allDevices);
        return PagedInfoList.fromPagedList("devices", deviceInfos, queryParameters);
    }

    @POST @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE)
    public Response addDevice(DeviceInfo info, @Context SecurityContext securityContext) {
        Device newDevice = newDevice(info.deviceConfigurationId, info.batch, info.name, info.serialNumber, info.yearOfCertification, info.shipmentDate);
        return Response.status(Response.Status.CREATED).entity(deviceInfoFactory.from(newDevice, getSlaveDevicesForDevice(newDevice))).build();
    }

    private Device newDevice(long deviceConfigurationId, String batch, String name, String serialNumber, int yearOfCertification, Instant shipmentDate) {
        Optional<DeviceConfiguration> deviceConfiguration = deviceConfigurationService.findDeviceConfiguration(deviceConfigurationId);
        Device newDevice;
        if (!is(batch).emptyOrOnlyWhiteSpace()) {
            newDevice = deviceService.newDevice(deviceConfiguration.orElse(null), name, batch, shipmentDate);
        } else {
            newDevice = deviceService.newDevice(deviceConfiguration.orElse(null), name, shipmentDate);
        }
        newDevice.setSerialNumber(serialNumber);
        newDevice.setYearOfCertification(yearOfCertification);
        newDevice.save();
        newDevice.getCurrentMeterActivation().ifPresent(meterActivation -> newDevice.getLifecycleDates().setReceivedDate(meterActivation.getStart()).save());
        return newDevice;
    }

    @PUT @Transactional
    @Path("/changedeviceconfig")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION)
    public Response changeDeviceConfig(BulkRequestInfo request, @BeanParam JsonQueryFilter queryFilter, @Context SecurityContext securityContext) {
        if (request.action == null || (!"ChangeDeviceConfiguration".equalsIgnoreCase(request.action))) {
            throw exceptionFactory.newException(MessageSeeds.BAD_ACTION);
        }
        DeviceConfiguration destinationConfiguration = deviceConfigurationService.findDeviceConfiguration(request.newDeviceConfiguration)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_CONFIG));
        DevicesForConfigChangeSearch devicesForConfigChangeSearch;
        if (request.filter != null) {
            devicesForConfigChangeSearch = devicesForConfigChangeSearchFactory.fromQueryFilter(new JsonQueryFilter(request.filter));
        } else {
            devicesForConfigChangeSearch = devicesForConfigChangeSearchFactory.fromQueryFilter(queryFilter);
        }
        deviceService.changeDeviceConfigurationForDevices(destinationConfiguration,
                devicesForConfigChangeSearch,
                request.deviceIds.stream().toArray(Long[]::new));
        return Response.ok().build();
    }

    @PUT//the method designed like 'PATCH'
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION)
    public Response updateDevice(@PathParam("id") long id, DeviceInfo info) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(info.name);
        if (device.getDeviceConfiguration().getId() != info.deviceConfigurationId) {
            DeviceConfiguration destinationConfiguration = deviceConfigurationService.findDeviceConfiguration(info.deviceConfigurationId)
                    .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_CONFIG));
            try {
                device = updateDeviceConfig(device, info.version, destinationConfiguration, destinationConfiguration.getVersion());
            } catch (CannotChangeDeviceConfigStillUnresolvedConflicts e) {
                final long deviceConfigurationId = device.getDeviceConfiguration().getId();
                long conflictId = device.getDeviceType().getDeviceConfigConflictMappings().stream()
                        .filter(conflict -> conflict.getOriginDeviceConfiguration().getId() == deviceConfigurationId
                                && conflict.getDestinationDeviceConfiguration()
                                .getId() == destinationConfiguration.getId()).findFirst().orElseThrow(() -> e).getId();
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Pair.of("changeDeviceConfigConflict", conflictId).asMap())
                        .build();
            }
        } else {
            try (TransactionContext context = transactionService.getContext()) {
                device = resourceHelper.lockDeviceOrThrowException(info);
                updateGateway(info, device);
                updateDataLoggerChannels(info, device);
                device.save();
                context.commit();
            }
        }
        return Response.ok().entity(deviceInfoFactory.from(device, getSlaveDevicesForDevice(device))).build();
    }

    public Device updateDeviceConfig(Device device, long deviceVersion, DeviceConfiguration destinationConfiguration, long deviceConfigurationVersion) {
        return deviceService.changeDeviceConfigurationForSingleDevice(device.getId(), deviceVersion, destinationConfiguration.getId(), deviceConfigurationVersion);
    }

    private void updateGateway(DeviceInfo info, Device device) {
        if (info.masterDeviceName != null) {
            updateGateway(device, info.masterDeviceName);
        } else {
            removeGateway(device);
        }
    }

    private void updateDataLoggerChannels(DeviceInfo info, Device dataLogger){
        if (dataLogger.getDeviceConfiguration().isDataloggerEnabled()) {
            List<Device> currentSlaves = topologyService.findDataLoggerSlaves(dataLogger);

            info.dataLoggerSlaveDevices.stream()
                    .filter(DataLoggerSlaveDeviceInfo::unlinked)
                    .forEach(dataLoggerSlaveDeviceInfo -> currentSlaves.stream().filter(slave -> slave.getId() == dataLoggerSlaveDeviceInfo.id).findAny()
                            .ifPresent(slaveToRemove -> topologyService.clearDataLogger(slaveToRemove, Instant.ofEpochMilli(dataLoggerSlaveDeviceInfo.unlinkingTimeStamp))));
            info.dataLoggerSlaveDevices.stream().filter(((Predicate<DataLoggerSlaveDeviceInfo>) DataLoggerSlaveDeviceInfo::unlinked).negate()).forEach((slaveDeviceInfo) -> setDataLogger(slaveDeviceInfo, dataLogger));
        }
    }

    private void setDataLogger(DataLoggerSlaveDeviceInfo slaveDeviceInfo, Device dataLogger){
        if (!slaveDeviceInfo.placeHolderForUnlinkedDataLoggerChannelsAndRegisters()) {
            Device slave;
            if (slaveDeviceInfo.id == 0 && slaveDeviceInfo.version == 0) {
                validateBeforeCreatingNewSlaveViaWizard(slaveDeviceInfo.name);
                slave = newDevice(slaveDeviceInfo.deviceConfigurationId, slaveDeviceInfo.batch, slaveDeviceInfo.name,
                        slaveDeviceInfo.serialNumber, slaveDeviceInfo.yearOfCertification, Instant.ofEpochMilli(slaveDeviceInfo.shipmentDate));
            } else {
                if (slaveDeviceInfo.isFromExistingLink()) {
                    // No new link, came along with deviceinfo
                    return;
                }
                slave = deviceService.findDeviceByName(slaveDeviceInfo.name)
                        .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_DEVICE, slaveDeviceInfo.name));
            }
            final Map<Channel, Channel> channelMap = new HashMap<>();
            if (slaveDeviceInfo.dataLoggerSlaveChannelInfos != null) {
                slaveDeviceInfo.dataLoggerSlaveChannelInfos.stream()
                        .map(info -> slaveDataLoggerChannelPair(slave, info))
                        .forEach((pair) -> channelMap.put(pair.getFirst(), pair.getLast()));
            }
            final HashMap<Register, Register> registerMap = new HashMap<>();
            if (slaveDeviceInfo.dataLoggerSlaveRegisterInfos != null) {
                slaveDeviceInfo.dataLoggerSlaveRegisterInfos.stream()
                        .map(info -> slaveDataLoggerRegisterPair(slave, info))
                        .forEach((pair) -> registerMap.put(pair.getFirst(), pair.getLast()));
            }
            if (channelMap.size() + registerMap.size() > 0) {
                topologyService.setDataLogger(slave, dataLogger, Instant.ofEpochMilli(slaveDeviceInfo.linkingTimeStamp), channelMap, registerMap);
            }
        }
    }

    /**
     * Validates the uniqueness of the name when creating a datalogger slave via the wizard.
     * We do the validation here because it doesn't properly work with form-validation
     *
     * @param deviceName the name of the new-to-create datalogger slave
     */
    private void validateBeforeCreatingNewSlaveViaWizard(String deviceName) {
        Optional<Device> existingDevice = deviceService.findDeviceByName(deviceName);
        if (existingDevice.isPresent()) {
            throw exceptionFactory.newException(MessageSeeds.UNIQUE_NAME);
        }
    }

    private Pair<Channel, Channel> slaveDataLoggerChannelPair(Device slave, DataLoggerSlaveChannelInfo info){
       return Pair.of(channelInfoToChannel(slave, info.slaveChannel), channelInfoToChannel(info.dataLoggerChannel));
    }

    private Optional<DataLoggerSlaveDeviceInfo> getTerminatedSlaveDeviceInfo(Device slave, DeviceInfo info){
        return info.dataLoggerSlaveDevices.stream().filter((dataLoggerSlaveDeviceInfo) -> dataLoggerSlaveDeviceInfo.id == slave.getId() && dataLoggerSlaveDeviceInfo.unlinked()).findFirst();
    }

    private Pair<Register, Register> slaveDataLoggerRegisterPair(Device slave, DataLoggerSlaveRegisterInfo info){
       return Pair.of(registerInfoToRegister(slave, info.slaveRegister), registerInfoToRegister(info.dataLoggerRegister));
    }

    private Channel channelInfoToChannel(ChannelInfo info){
        return resourceHelper.findChannelOnDeviceOrThrowException(info.parent.id, info.id );
    }

    private Channel channelInfoToChannel(Device device, ChannelInfo info){
        return resourceHelper.findChannelOnDeviceOrThrowException(device, info.id );
    }

    private Register registerInfoToRegister(RegisterInfo info){
        return resourceHelper.findRegisterOnDeviceOrThrowException(info.deviceName, info.id);
    }

    private Register registerInfoToRegister(Device slave, RegisterInfo info){
        return resourceHelper.findRegisterOnDeviceOrThrowException(slave, info.id);
    }

    private void updateGateway(Device device, String gatewayName) {
        if (device.getDeviceConfiguration().isDirectlyAddressable()) {
            throw exceptionFactory.newException(MessageSeeds.IMPOSSIBLE_TO_SET_MASTER_DEVICE, device.getName());
        }
        Optional<Device> currentGateway = topologyService.getPhysicalGateway(device);
        if (!currentGateway.isPresent() || !currentGateway.get().getName().equals(gatewayName)) {
            Device newGateway = resourceHelper.findDeviceByNameOrThrowException(gatewayName);
            topologyService.setPhysicalGateway(device, newGateway);
        }
    }

    private void removeGateway(Device device) {
        if (!device.getDeviceType().isDataloggerSlave()){
            if (topologyService.getPhysicalGateway(device).isPresent()) {
                topologyService.clearPhysicalGateway(device);
            }
        }
    }

    private List<DeviceTopologyInfo> getSlaveDevicesForDevice(Device device) {
        List<DeviceTopologyInfo> slaves;
        if (GatewayType.LOCAL_AREA_NETWORK.equals(device.getConfigurationGatewayType())) {
            slaves = DeviceTopologyInfo.from(topologyService.getPhysicalTopologyTimelineAdditions(device, RECENTLY_ADDED_COUNT), deviceLifeCycleConfigurationService);
        } else {
            slaves = DeviceTopologyInfo.from(topologyService.getPysicalTopologyTimeline(device), deviceLifeCycleConfigurationService);
        }
        return slaves;
    }

    @GET @Transactional
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public DeviceInfo findDeviceByName(@PathParam("name") String name, @Context SecurityContext securityContext) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        return deviceInfoFactory.from(device, getSlaveDevicesForDevice(device));
    }

    @GET @Transactional
    @Path("/{name}/attributes")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE})
    public Response getDeviceAttributes(@PathParam("name") String name) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        return Response.ok(deviceAttributesInfoFactory.from(device)).build();
    }

    @PUT @Transactional
    @Path("/{name}/attributes")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_ATTRIBUTE})
    public Response editDeviceAttributes(@PathParam("name") String name, DeviceAttributesInfo info) {
        Device device = resourceHelper.lockDeviceOrThrowException(info.device);
        deviceAttributesInfoFactory.validateOn(device, info);
        deviceAttributesInfoFactory.writeTo(device, info);
        return Response.ok(deviceAttributesInfoFactory.from(device)).build();
    }

    @GET @Transactional
    @Path("/{name}/customproperties")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE})
    public PagedInfoList getDeviceCustomProperties(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        List<CustomPropertySetInfo> customPropertySetInfos = resourceHelper.getDeviceCustomPropertySetInfos(device);
        return PagedInfoList.fromCompleteList("customproperties", customPropertySetInfos, queryParameters);
    }

    @GET @Transactional
    @Path("/{name}/customproperties/{cpsId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE})
    public CustomPropertySetInfo getDeviceCustomProperty(@PathParam("name") String name, @PathParam("cpsId") long cpsId, @QueryParam("default") boolean defaultValues) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        if(defaultValues){
            return resourceHelper.getDeviceCustomPropertySetInfoWithDefaultValues(device, cpsId);
        } else {
            return resourceHelper.getDeviceCustomPropertySetInfos(device)
                    .stream()
                    .filter(f -> f.id == cpsId)
                    .findFirst()
                    .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_CUSTOMPROPERTYSET, cpsId));
        }
    }

    @GET @Transactional
    @Path("/{name}/customproperties/{cpsId}/versions")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE})
    public PagedInfoList getDeviceCustomPropertyVersioned(@PathParam("name") String name, @PathParam("cpsId") long cpsId, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        List<CustomPropertySetInfo> customPropertySetInfoList = resourceHelper.getVersionedCustomPropertySetHistoryInfos(device, cpsId);
        return PagedInfoList.fromCompleteList("versions", customPropertySetInfoList, queryParameters);
    }

    @GET @Transactional
    @Path("/{name}/customproperties/{cpsId}/versions/{timeStamp}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE})
    public CustomPropertySetInfo getDeviceCustomPropertyVersionedHistory(@PathParam("name") String name, @PathParam("cpsId") long cpsId, @PathParam("timeStamp") Long timeStamp) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        return resourceHelper.getDeviceCustomPropertySetInfos(device, Instant.ofEpochMilli(timeStamp))
                .stream()
                .filter(f -> f.id == cpsId)
                .findFirst()
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_CUSTOMPROPERTYSET, cpsId));
    }

    @GET @Transactional
    @Path("/{name}/customproperties/{cpsId}/currentinterval")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public IntervalInfo getCurrentTimeInterval(@PathParam("name") String name, @PathParam("cpsId") long cpsId) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        Interval interval = Interval.of(resourceHelper.getCurrentTimeInterval(device, cpsId));

        return IntervalInfo.from(interval.toClosedOpenRange());
    }

    @GET @Transactional
    @Path("/{name}/customproperties/{cpsId}/conflicts")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public PagedInfoList getOverlaps(@PathParam("name") String name, @PathParam("cpsId") long cpsId, @QueryParam("startTime") long startTime, @QueryParam("endTime") long endTime, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        List<CustomPropertySetIntervalConflictInfo> overlapInfos = resourceHelper.getOverlapsWhenCreate(device, cpsId, resourceHelper.getTimeRange(startTime, endTime));
        Collections.sort(overlapInfos, resourceHelper.getConflictInfosComparator());
        return PagedInfoList.fromCompleteList("conflicts", overlapInfos, queryParameters);
    }

    @GET @Transactional
    @Path("/{name}/customproperties/{cpsId}/conflicts/{timeStamp}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public PagedInfoList getOverlaps(@PathParam("name") String name, @PathParam("cpsId") long cpsId, @PathParam("timeStamp") long timeStamp, @QueryParam("startTime") long startTime, @QueryParam("endTime") long endTime, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        List<CustomPropertySetIntervalConflictInfo> overlapInfos = resourceHelper.getOverlapsWhenUpdate(device, cpsId, resourceHelper.getTimeRange(startTime, endTime), Instant.ofEpochMilli(timeStamp));
        Collections.sort(overlapInfos, resourceHelper.getConflictInfosComparator());
        return PagedInfoList.fromCompleteList("conflicts", overlapInfos, queryParameters);
    }

    @PUT @Transactional
    @Path("/{name}/customproperties/{cpsId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public Response editDeviceCustomAttribute(@PathParam("name") String name, @PathParam("cpsId") long cpsId, CustomPropertySetInfo customPropertySetInfo) {
        Device lockedDevice = resourceHelper.lockDeviceOrThrowException(customPropertySetInfo.parent, name, customPropertySetInfo.version);
        resourceHelper.lockDeviceTypeOrThrowException(customPropertySetInfo.objectTypeId, customPropertySetInfo.objectTypeVersion);
        resourceHelper.setDeviceCustomPropertySetInfo(lockedDevice, cpsId, customPropertySetInfo);
        return Response.ok().build();
    }

    @POST @Transactional
    @Path("/{name}/customproperties/{cpsId}/versions")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA, Privileges.Constants.ADMINISTER_DEVICE_TIME_SLICED_CPS})
    public Response addDeviceCustomAttributeVersioned(@PathParam("name") String name, @PathParam("cpsId") long cpsId, @QueryParam("forced") boolean forced, CustomPropertySetInfo customPropertySetInfo) {
        Device lockedDevice = resourceHelper.lockDeviceOrThrowException(customPropertySetInfo.parent, name, customPropertySetInfo.version);
        resourceHelper.lockDeviceTypeOrThrowException(customPropertySetInfo.objectTypeId, customPropertySetInfo.objectTypeVersion);
        Optional<IntervalErrorInfos> intervalErrors = resourceHelper.verifyTimeRange(customPropertySetInfo.startTime, customPropertySetInfo.endTime);
        if (intervalErrors.isPresent()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(intervalErrors.get()).build();
        }
        List<CustomPropertySetIntervalConflictInfo> overlapInfos =
                resourceHelper.getOverlapsWhenCreate(lockedDevice, cpsId, resourceHelper.getTimeRange(customPropertySetInfo.startTime, customPropertySetInfo.endTime))
                        .stream()
                        .filter(e -> !e.conflictType.equals(ValuesRangeConflictType.RANGE_INSERTED.name()))
                        .filter(resourceHelper.filterGaps(forced))
                        .collect(Collectors.toList());
        if (!overlapInfos.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new CustomPropertySetIntervalConflictErrorInfo(overlapInfos.stream().collect(Collectors.toList())))
                    .build();
        }
        resourceHelper.addDeviceCustomPropertySetVersioned(lockedDevice, cpsId, customPropertySetInfo);
        return Response.ok().build();
    }

    @PUT @Transactional
    @Path("/{name}/customproperties/{cpsId}/versions/{timeStamp}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA, Privileges.Constants.ADMINISTER_DEVICE_TIME_SLICED_CPS})
    public Response editDeviceCustomAttributeVersioned(@PathParam("name") String name, @PathParam("cpsId") long cpsId, @PathParam("timeStamp") long timeStamp, @QueryParam("forced") boolean forced, CustomPropertySetInfo customPropertySetInfo) {
        Device lockedDevice = resourceHelper.lockDeviceOrThrowException(customPropertySetInfo.parent, name, customPropertySetInfo.version);
        resourceHelper.lockDeviceTypeOrThrowException(customPropertySetInfo.objectTypeId, customPropertySetInfo.objectTypeVersion);
        Optional<IntervalErrorInfos> intervalErrors = resourceHelper.verifyTimeRange(customPropertySetInfo.startTime, customPropertySetInfo.endTime);
        if (intervalErrors.isPresent()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(intervalErrors.get()).build();
        }
        List<CustomPropertySetIntervalConflictInfo> overlapInfos =
                resourceHelper.getOverlapsWhenUpdate(lockedDevice, cpsId, resourceHelper.getTimeRange(customPropertySetInfo.startTime, customPropertySetInfo.endTime), Instant.ofEpochMilli(timeStamp))
                        .stream()
                        .filter(e -> !e.conflictType.equals(ValuesRangeConflictType.RANGE_INSERTED.name()))
                        .filter(resourceHelper.filterGaps(forced))
                        .collect(Collectors.toList());
        if (!overlapInfos.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new CustomPropertySetIntervalConflictErrorInfo(overlapInfos.stream().collect(Collectors.toList())))
                    .build();
        }
        resourceHelper.setDeviceCustomPropertySetVersioned(lockedDevice, cpsId, customPropertySetInfo, Instant.ofEpochMilli(timeStamp));
        return Response.ok().build();
    }

    @GET @Transactional
    @Path("/{name}/privileges")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE})
    public Response getDeviceConstraintsBasedOnDeviceState(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters, @Context SecurityContext securityContext) {
        User user = (User) securityContext.getUserPrincipal();
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        List<IdWithNameInfo> privileges = DevicePrivileges.getPrivilegesFor(device, user)
                .stream()
                .map(privilege -> new IdWithNameInfo(null, privilege))
                .collect(Collectors.toList());
        privileges.addAll(DevicePrivileges.getTimeOfUsePrivilegesFor(device, deviceConfigurationService)
                .stream()
                .map(privilege -> new IdWithNameInfo(null, privilege))
                .collect(Collectors.toList()));
        return Response.ok(PagedInfoList.fromCompleteList("privileges", privileges, queryParameters)).build();
    }

    /**
     * List all device message categories with devices messages:
     * - that are supported by the device protocol defined on the device's device type
     * - that have device messages specs on them (enablement)
     * - that the user has the required privileges for
     *
     * @param name Device's name
     * @return List of categories + device message specs, indicating if message spec will be picked up by a comtask or not
     */
    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{name}/messagecategories")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA,
            com.energyict.mdc.device.config.security.Privileges.Constants.EXECUTE_DEVICE_MESSAGE_1,
            com.energyict.mdc.device.config.security.Privileges.Constants.EXECUTE_DEVICE_MESSAGE_2,
            com.energyict.mdc.device.config.security.Privileges.Constants.EXECUTE_DEVICE_MESSAGE_3,
            com.energyict.mdc.device.config.security.Privileges.Constants.EXECUTE_DEVICE_MESSAGE_4})
    public PagedInfoList getAllAvailableDeviceCategoriesIncludingMessageSpecsForCurrentUser(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters, @Context UriInfo uriInfo) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        List<DeviceMessageCategoryInfo> infos = new ArrayList<>();
        deviceMessageSpecificationService
                .filteredCategoriesForUserSelection()
                .stream()
                .sorted((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()))
                .forEach(category -> {
                    List<DeviceMessageSpecInfo> deviceMessageSpecs =
                            device
                                .getDeviceConfiguration()
                                .getEnabledAndAuthorizedDeviceMessageSpecsIn(category)
                                .stream()
                                .sorted(Comparator.comparing(DeviceMessageSpec::getName))
                                .map(dms -> deviceMessageSpecInfoFactory.asInfoWithMessagePropertySpecs(dms, device))
                                .collect(Collectors.toList());
                    if (!deviceMessageSpecs.isEmpty()) {
                        DeviceMessageCategoryInfo info = deviceMessageCategoryInfoFactory.asInfo(category);
                        info.deviceMessageSpecs = deviceMessageSpecs;
                        infos.add(info);
                    }
                });
        return PagedInfoList
                .fromPagedList(
                        "categories",
                        ListPager.of(infos).from(queryParameters).find(),
                        queryParameters);
    }

    @Path("/{name}/connectionmethods")
    public ConnectionMethodResource getConnectionMethodResource() {
        return connectionMethodResourceProvider.get();
    }

    @Path("/{name}/protocoldialects")
    public ProtocolDialectResource getProtocolDialectsResource() {
        return protocolDialectResourceProvider.get();
    }

    @Path("/{name}/registers")
    public RegisterResource getRegisterResource() {
        return registerResourceProvider.get();
    }

    @Path("/{name}/channels")
    public ChannelResource getChannelResource() {
        return channelsOnDeviceResourceProvider.get();
    }

    @Path("/{name}/validationrulesets")
    public DeviceValidationResource getDeviceValidationResource() {
        return deviceValidationResourceProvider.get();
    }

    @Path("/{name}/estimationrulesets")
    public DeviceEstimationResource getDeviceEstimationResource() {
        return deviceEstimationResourceProvider.get();
    }

    @Path("/{name}/loadprofiles")
    public LoadProfileResource getLoadProfileResource() {
        return loadProfileResourceProvider.get();
    }

    @Path("/{name}/logbooks")
    public LogBookResource getLogBookResource() {
        return logBookResourceProvider.get();
    }

    @Path("/{name}/whatsgoingon")
    public GoingOnResource getGoingOnResource() {
        return goingOnResourceProvider.get();
    }

    @Path("/schedules")
    public BulkScheduleResource getBulkScheduleResource() {
        return bulkScheduleResourceProvider.get();
    }

    @Path("/{name}/schedules")
    public DeviceScheduleResource getComTaskExecutionResource() {
        return deviceScheduleResourceProvider.get();
    }

    @Path("/{name}/comtasks")
    public DeviceComTaskResource getComTaskResource() {
        return deviceComTaskResourceProvider.get();
    }

    @Path("/{name}/devicemessages")
    public DeviceMessageResource getCommandResource() {
        return deviceCommandResourceProvider.get();
    }

    @Path("/{name}/securityproperties")
    public SecurityPropertySetResource getSecurityPropertySetResource() {
        return securityPropertySetResourceProvider.get();
    }

    @Path("/{name}/protocols")
    public DeviceProtocolPropertyResource getDevicePropertyResource(@PathParam("name") String name) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        return devicePropertyResourceProvider.get().with(device);
    }

    @Path("/{name}/devicelabels")
    public DeviceLabelResource getDeviceLabelResource() {
        return deviceLabelResourceProvider.get();
    }

    @Path("/{name}/connections")
    public ConnectionResource getConnectionResource() {
        return connectionResourceProvider.get();
    }

    @Path("/{name}/history")
    public DeviceHistoryResource getDeviceHistoryResource() {
        return deviceHistoryResourceProvider.get();
    }

    @Path("/{name}/transitions")
    public DeviceLifeCycleActionResource getDeviceLifeCycleActionsResource() {
        return deviceLifeCycleActionResourceProvider.get();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{name}/runningservicecalls")
    public PagedInfoList getServiceCallsFor(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        List<ServiceCallInfo> serviceCallInfos = new ArrayList<>();
        Set<DefaultState> states = EnumSet.of(
                DefaultState.CREATED,
                DefaultState.SCHEDULED,
                DefaultState.PENDING,
                DefaultState.PAUSED,
                DefaultState.ONGOING,
                DefaultState.WAITING);

        serviceCallService.findServiceCalls(device, states).forEach(serviceCall -> serviceCallInfos.add(serviceCallInfoFactory.summarized(serviceCall)));

        return PagedInfoList.fromCompleteList("serviceCalls", serviceCallInfos, queryParameters);
    }

    @PUT
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{name}/runningservicecalls/{id}")
    public Response cancelServiceCall(@PathParam("name") String name, @PathParam("id") long serviceCallId, ServiceCallInfo info) {
        if ("sclc.default.cancelled".equals(info.state.id)) {
            serviceCallService.getServiceCall(serviceCallId).ifPresent(ServiceCall::cancel);
            return Response.status(Response.Status.ACCEPTED).build();
        }
        throw exceptionFactory.newException(MessageSeeds.BAD_REQUEST);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{name}/servicecallhistory")
    public PagedInfoList getServiceCallHistoryFor(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter jsonQueryFilter) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        List<ServiceCallInfo> serviceCallInfos = new ArrayList<>();

        ServiceCallFilter filter = serviceCallInfoFactory.convertToServiceCallFilter(jsonQueryFilter);
        serviceCallService.getServiceCallFinder(filter)
                .stream()
                .filter(serviceCall -> serviceCall.getTargetObject().map(device::equals).orElse(false))
                .forEach(serviceCall -> serviceCallInfos.add(serviceCallInfoFactory.summarized(serviceCall)));

        return PagedInfoList.fromCompleteList("serviceCalls", serviceCallInfos, queryParameters);
    }

    @PUT
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{name}/servicecalls")
    public Response cancelServiceCallsFor(@PathParam("name") String name, ServiceCallInfo serviceCallInfo) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        if (serviceCallInfo.state == null) {
            throw exceptionFactory.newException(MessageSeeds.BAD_REQUEST);
        }
        if (DefaultState.CANCELLED.getKey().equals(serviceCallInfo.state.id)) {
            serviceCallService.cancelServiceCallsFor(device);
            return Response.accepted().build();
        }
        throw exceptionFactory.newException(MessageSeeds.BAD_REQUEST);
    }

    @GET @Transactional
    @Path("/{name}/topology/communication")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA,
            com.energyict.mdc.device.config.security.Privileges.Constants.EXECUTE_DEVICE_MESSAGE_1,
            com.energyict.mdc.device.config.security.Privileges.Constants.EXECUTE_DEVICE_MESSAGE_2,
            com.energyict.mdc.device.config.security.Privileges.Constants.EXECUTE_DEVICE_MESSAGE_3,
            com.energyict.mdc.device.config.security.Privileges.Constants.EXECUTE_DEVICE_MESSAGE_4})
    public PagedInfoList getCommunicationReferences(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter filter) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        TopologyTimeline timeline = topologyService.getPysicalTopologyTimeline(device);
        Predicate<Device> filterPredicate = getFilterForCommunicationTopology(filter);
        Stream<Device> stream = timeline.getAllDevices().stream().filter(filterPredicate).sorted(Comparator.comparing(Device::getName));
        if (queryParameters.getStart().isPresent() && queryParameters.getStart().get() > 0) {
            stream = stream.skip(queryParameters.getStart().get());
        }
        List<DeviceTopologyInfo> topologyList = stream.map(d -> DeviceTopologyInfo.from(d, timeline.mostRecentlyAddedOn(d), deviceLifeCycleConfigurationService))
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("slaveDevices", topologyList, queryParameters);
    }

    @GET @Transactional
    @Path("/{name}/dataloggerslaves")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getDataLoggerSlaves(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter filter) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        return PagedInfoList.fromCompleteList("dataLoggerSlaveDevices",  getDataLoggerSlavesForDevice(device), queryParameters);
    }

    private List<DeviceTopologyInfo> getDataLoggerSlavesForDevice(Device device) {
        return (device.getDeviceConfiguration().isDataloggerEnabled() ? resourceHelper.getDataLoggerSlaves(device): Collections.emptyList());
    }

    @GET @Transactional
    @Path("/unlinkeddataloggerslaves")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public DataLoggerSlaveDeviceInfos getUnlinkedSlaves(@BeanParam JsonQueryParameters queryParameters) {
        String searchText = queryParameters.getLike();
        if (searchText != null && !searchText.isEmpty()) {
            return dataLoggerSlaveDeviceInfoFactory.forDataLoggerSlaves(deviceService.findAllDevices(getUnlinkedSlaveDevicesCondition(searchText))
                    .stream()
                    .limit(50)
                    .collect(Collectors.toList()));
        }
        return dataLoggerSlaveDeviceInfoFactory.forDataLoggerSlaves(Collections.emptyList());
    }

    private Condition getUnlinkedSlaveDevicesCondition(String dbSearchText) {
        // a. Datalogger slave devices
        String regex = '*' + dbSearchText.replace(' ', '*') + '*';
        Condition a = Where.where("name").likeIgnoreCase(regex)
            .and(Where.where("deviceType.deviceTypePurpose").isEqualTo(DeviceTypePurpose.DATALOGGER_SLAVE));
        // b. that are not linked yet to a data logger
        Condition b = ListOperator.NOT_IN.contains(topologyService.findAllEffectiveDataLoggerSlaveDevices().asSubQuery("origin"), "id");
        return a.and(b);
    }

    @GET
    @Transactional
    @Path("/{name}/timeofuse")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.VIEW_DEVICE)
    public Response getCalendarInfo(@PathParam("name") String name) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        TimeOfUseInfo info = timeOfUseInfoFactory.from(device);
        return Response.ok(info).build();
    }

    @POST
    @Transactional
    @Path("/{name}/timeofuse/send")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.config.security.Privileges.Constants.EXECUTE_DEVICE_MESSAGE_1,
            com.energyict.mdc.device.config.security.Privileges.Constants.EXECUTE_DEVICE_MESSAGE_2,
            com.energyict.mdc.device.config.security.Privileges.Constants.EXECUTE_DEVICE_MESSAGE_3,
            com.energyict.mdc.device.config.security.Privileges.Constants.EXECUTE_DEVICE_MESSAGE_4})
    public Response sendCalendar(@PathParam("name") String name, SendCalendarInfo sendCalendarInfo) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        Set<ProtocolSupportedCalendarOptions> allowedOptions = getAllowedTimeOfUseOptions(device);
        AllowedCalendar calendar = device.getDeviceType().getAllowedCalendars().stream()
                .filter(allowedCalendar -> !allowedCalendar.isGhost() && allowedCalendar.getId() == sendCalendarInfo.allowedCalendarId)
                .findFirst().orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.UNABLE_TO_FIND_CALENDAR));
        DeviceMessageId deviceMessageId = getDeviceMessageId(sendCalendarInfo, allowedOptions)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_ALLOWED_CALENDAR_DEVICE_MESSAGE));
        DeviceMessage<Device> deviceMessage = sendNewMessage(device, deviceMessageId, sendCalendarInfo, calendar);
        device.calendars().setPassive(calendar, sendCalendarInfo.activationDate, deviceMessage);
        return Response.ok().build();
    }


    @GET
    @Path("/{name}/timeofuse/{calendarId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.VIEW_DEVICE)
    public Response getCalendar(@PathParam("name") String name, @PathParam("calendarId") long calendarId, @QueryParam("weekOf") long milliseconds) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        ActiveEffectiveCalendar activeCalendar = device.calendars().getActive().orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.UNABLE_TO_FIND_CALENDAR));
        AllowedCalendar allowedCalendar = activeCalendar.getAllowedCalendar();
        Calendar calendar = allowedCalendar.getCalendar().orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.UNABLE_TO_FIND_CALENDAR));
        if (calendar.getId() != calendarId) {
            throw exceptionFactory.newException(MessageSeeds.CALENDAR_NOT_ACTIVE_ON_DEVICE);
        }
        if (milliseconds <= 0) {
            return Response.ok(calendarService.findCalendar(calendarId)
                    .map(calendarInfoFactory::detailedFromCalendar)
                    .orElseThrow(IllegalArgumentException::new)).build();
        } else {
            Instant instant = Instant.ofEpochMilli(milliseconds);
            LocalDate localDate = LocalDateTime.ofInstant(instant, ZoneId.of("UTC"))
                    .toLocalDate();

            return Response.ok(transformToWeekCalendar(calendar, localDate)).build();
        }
    }

    @GET
    @Path("/{name}/timeofuse/availablecalendars")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.config.security.Privileges.Constants.EXECUTE_DEVICE_MESSAGE_1,
            com.energyict.mdc.device.config.security.Privileges.Constants.EXECUTE_DEVICE_MESSAGE_2,
            com.energyict.mdc.device.config.security.Privileges.Constants.EXECUTE_DEVICE_MESSAGE_3,
            com.energyict.mdc.device.config.security.Privileges.Constants.EXECUTE_DEVICE_MESSAGE_4})
    public Response getAvailableCalendars(@PathParam("name") String name) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        List<IdWithNameInfo> calendars = device.getDeviceConfiguration().getDeviceType().getAllowedCalendars().stream()
                .filter(allowedCalendar -> !allowedCalendar.isGhost())
                .map(allowedCalendar -> new IdWithNameInfo(allowedCalendar.getId(), allowedCalendar.getName()))
                .collect(Collectors.toList());

        return Response.ok(calendars).build();
    }

    @PUT
    @Path("/{name}/timeofuse/verify")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed({com.energyict.mdc.device.config.security.Privileges.Constants.EXECUTE_DEVICE_MESSAGE_1,
            com.energyict.mdc.device.config.security.Privileges.Constants.EXECUTE_DEVICE_MESSAGE_2,
            com.energyict.mdc.device.config.security.Privileges.Constants.EXECUTE_DEVICE_MESSAGE_3,
            com.energyict.mdc.device.config.security.Privileges.Constants.EXECUTE_DEVICE_MESSAGE_4})
    public Response verifyCalendar(@PathParam("name") String name) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        try {
            device.runStatusInformationTask(ComTaskExecution::runNow);
        } catch (NoStatusInformationTaskException e) {
            throw exceptionFactory.newException(MessageSeeds.VERIFY_CALENDAR_TASK_IS_NOT_ACTIVE);
        }
        return Response.status(Response.Status.ACCEPTED).build();
    }

    @PUT
    @Path("/{name}/timeofuse/clearpassive")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed({com.energyict.mdc.device.config.security.Privileges.Constants.EXECUTE_DEVICE_MESSAGE_1,
            com.energyict.mdc.device.config.security.Privileges.Constants.EXECUTE_DEVICE_MESSAGE_2,
            com.energyict.mdc.device.config.security.Privileges.Constants.EXECUTE_DEVICE_MESSAGE_3,
            com.energyict.mdc.device.config.security.Privileges.Constants.EXECUTE_DEVICE_MESSAGE_4})
    public Response clearPassiveCalendar(@PathParam("name") String name) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        Set<ProtocolSupportedCalendarOptions> allowedOptions = getAllowedTimeOfUseOptions(device);
        if (!allowedOptions.contains(ProtocolSupportedCalendarOptions.CLEAR_AND_DISABLE_PASSIVE_TARIFF)) {
            throw exceptionFactory.newException(MessageSeeds.COMMAND_NOT_ALLOWED_OR_SUPPORTED);
        }
        DeviceMessage<Device> deviceMessage = device.newDeviceMessage(DeviceMessageId.ACTIVITY_CALENDAR_CLEAR_AND_DISABLE_PASSIVE_TARIFF).setReleaseDate(Instant.now(clock)).add();
        boolean willBePickedUpByPlannedComtask = deviceMessageService.willDeviceMessageBePickedUpByPlannedComTask(device, deviceMessage);
        boolean willBePickedUpByComtask = willBePickedUpByPlannedComtask || deviceMessageService.willDeviceMessageBePickedUpByComTask(device, deviceMessage);
        return Response.accepted(new PlannedDeviceMessageInfo(willBePickedUpByPlannedComtask, willBePickedUpByComtask)).build();
    }

    @PUT
    @Path("/{name}/timeofuse/activatepassive")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed({com.energyict.mdc.device.config.security.Privileges.Constants.EXECUTE_DEVICE_MESSAGE_1,
            com.energyict.mdc.device.config.security.Privileges.Constants.EXECUTE_DEVICE_MESSAGE_2,
            com.energyict.mdc.device.config.security.Privileges.Constants.EXECUTE_DEVICE_MESSAGE_3,
            com.energyict.mdc.device.config.security.Privileges.Constants.EXECUTE_DEVICE_MESSAGE_4})
    public Response activatePassiveCalendar(@PathParam("name") String name, @FormParam("activationDate") long activationDate) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        Set<ProtocolSupportedCalendarOptions> allowedOptions = getAllowedTimeOfUseOptions(device);
        if (!allowedOptions.contains(ProtocolSupportedCalendarOptions.ACTIVATE_PASSIVE_CALENDAR)) {
            throw exceptionFactory.newException(MessageSeeds.COMMAND_NOT_ALLOWED_OR_SUPPORTED);
        }
        DeviceMessage<Device> deviceMessage = device.newDeviceMessage(DeviceMessageId.ACTIVATE_CALENDAR_PASSIVE)
                .setReleaseDate(Instant.now(clock))
                .addProperty(DeviceMessageConstants.activityCalendarActivationDateAttributeName, Date.from(Instant.ofEpochMilli(activationDate)))
                .add();
        boolean willBePickedUpByPlannedComtask = deviceMessageService.willDeviceMessageBePickedUpByPlannedComTask(device, deviceMessage);
        boolean willBePickedUpByComtask = willBePickedUpByPlannedComtask || deviceMessageService.willDeviceMessageBePickedUpByComTask(device, deviceMessage);
        return Response.accepted(new PlannedDeviceMessageInfo(willBePickedUpByPlannedComtask, willBePickedUpByComtask)).build();
    }

    private CalendarInfo transformToWeekCalendar(Calendar calendar, LocalDate localDate) {
        return calendarInfoFactory.detailedWeekFromCalendar(calendar, localDate);
    }


    private Optional<DeviceMessageId> getDeviceMessageId(SendCalendarInfo sendCalendarInfo, Set<ProtocolSupportedCalendarOptions> allowedOptions) {
        CalendarUpdateOption calendarUpdateOption = CalendarUpdateOption.find(sendCalendarInfo.calendarUpdateOption);
        DeviceMessageId deviceMessageId;
        boolean hasActivationDate = sendCalendarInfo.activationDate != null;
        boolean hasType = sendCalendarInfo.type != null;
        boolean hasContract = sendCalendarInfo.contract != null;
        boolean hasActivityCalendarOption = CalendarUpdateOption.FULL_CALENDAR.equals(calendarUpdateOption);
        boolean hasSpecialDaysCalendarOption = CalendarUpdateOption.SPECIAL_DAYS.equals(calendarUpdateOption);
        boolean sendCalendarWithDateAllowed = allowedOptions.contains(ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATE);
        boolean sendCalendarWithDateTimeAllowed = allowedOptions.contains(ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATETIME);

        int index = 0;

        for (boolean flag : new boolean[]{hasActivationDate, hasType, hasContract}) {
            index <<= 1;
            index |= flag ? 1 : 0;
        }

        Supplier<DeviceMessageId>[] options = new Supplier[8];
        options[0b000] = () -> hasActivityCalendarOption ? ACTIVITY_CALENDER_SEND : (hasSpecialDaysCalendarOption ? ACTIVITY_CALENDAR_SPECIAL_DAY_CALENDAR_SEND : null);
        options[0b001] = () -> hasActivityCalendarOption ? ACTIVITY_CALENDER_SEND : (hasSpecialDaysCalendarOption ? ACTIVITY_CALENDAR_SPECIAL_DAY_CALENDAR_SEND : null);
        options[0b010] = () -> ACTIVITY_CALENDAR_SPECIAL_DAY_CALENDAR_SEND_WITH_TYPE;
        options[0b011] = () -> ACTIVITY_CALENDAR_SPECIAL_DAY_CALENDAR_SEND_WITH_TYPE;
        options[0b100] = () -> sendCalendarWithDateAllowed ? ACTIVITY_CALENDER_SEND_WITH_DATE : (sendCalendarWithDateTimeAllowed ? ACTIVITY_CALENDER_SEND_WITH_DATETIME : null);
        options[0b101] = () -> hasActivityCalendarOption ? ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_CONTRACT : (hasSpecialDaysCalendarOption ? ACTIVITY_CALENDER_SPECIAL_DAY_CALENDAR_SEND_WITH_CONTRACT_AND_DATETIME : null);
        options[0b110] = () -> ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_TYPE;
        options[0b111] = () -> ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_TYPE;

        deviceMessageId = options[index].get();

        return checkIfDeviceMessageIsAllowed(deviceMessageId, allowedOptions) ? Optional.of(deviceMessageId) : Optional.empty();
    }

    private boolean checkIfDeviceMessageIsAllowed(DeviceMessageId deviceMessageId, Set<ProtocolSupportedCalendarOptions> allowedOptions) {
        Map<DeviceMessageId, ProtocolSupportedCalendarOptions> messageId2Option = new HashMap<>();
        messageId2Option.put(ACTIVITY_CALENDER_SEND, ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR);
        messageId2Option.put(ACTIVITY_CALENDER_SEND_WITH_DATE, ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATE);
        messageId2Option.put(ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_TYPE, ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATE_AND_TYPE);
        messageId2Option.put(ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_CONTRACT, ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATE_AND_CONTRACT);
        messageId2Option.put(ACTIVITY_CALENDER_SEND_WITH_DATETIME, ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATETIME);
        messageId2Option.put(ACTIVITY_CALENDAR_SPECIAL_DAY_CALENDAR_SEND, ProtocolSupportedCalendarOptions.SEND_SPECIAL_DAYS_CALENDAR);
        messageId2Option.put(ACTIVITY_CALENDAR_SPECIAL_DAY_CALENDAR_SEND_WITH_TYPE, ProtocolSupportedCalendarOptions.SEND_SPECIAL_DAYS_CALENDAR_WITH_TYPE);
        messageId2Option.put(ACTIVITY_CALENDER_SPECIAL_DAY_CALENDAR_SEND_WITH_CONTRACT_AND_DATETIME, ProtocolSupportedCalendarOptions.SEND_SPECIAL_DAYS_CALENDAR_WITH_CONTRACT_AND_DATE);

        return allowedOptions.contains(messageId2Option.get(deviceMessageId));

    }

    private DeviceMessage<Device> sendNewMessage(Device device, DeviceMessageId deviceMessageId, SendCalendarInfo sendCalendarInfo, AllowedCalendar calendar) {
        Device.DeviceMessageBuilder messageBuilder = device.newDeviceMessage(deviceMessageId);

        if (isSpecialDays(deviceMessageId)) {
            messageBuilder.addProperty(DeviceMessageConstants.specialDaysAttributeName, calendar.getCalendar().get());
        } else {
            messageBuilder.addProperty(DeviceMessageConstants.activityCalendarNameAttributeName, calendar.getName())
                    .addProperty(DeviceMessageConstants.activityCalendarAttributeName, calendar.getCalendar().get());
        }

        if (sendCalendarInfo.releaseDate != null) {
            messageBuilder.setReleaseDate(sendCalendarInfo.releaseDate);
        }
        if (needsActivationDate(deviceMessageId)) {
            Date date = Date.from(sendCalendarInfo.activationDate);
            messageBuilder.addProperty(DeviceMessageConstants.activityCalendarActivationDateAttributeName,
                    date);
        }
        if (needsType(deviceMessageId)) {
            messageBuilder.addProperty(DeviceMessageConstants.activityCalendarTypeAttributeName,
                    sendCalendarInfo.type);
        }
        if (needsContract(deviceMessageId)) {
            messageBuilder.addProperty(DeviceMessageConstants.contractAttributeName,
                    sendCalendarInfo.contract);
        }
        return messageBuilder.add();
    }


    private Set<ProtocolSupportedCalendarOptions> getAllowedTimeOfUseOptions(Device device) {
        Optional<TimeOfUseOptions> timeOfUseOptions = deviceConfigurationService.findTimeOfUseOptions(device.getDeviceConfiguration().getDeviceType());
        return timeOfUseOptions.map(TimeOfUseOptions::getOptions).orElse(Collections.emptySet());
    }

    private Predicate<Device> getFilterForCommunicationTopology(JsonQueryFilter filter) {
        Predicate<Device> predicate = d -> true;
        predicate = addPropertyStringFilterIfAvailabale(filter, "name", predicate, Device::getName);
        predicate = addPropertyStringFilterIfAvailabale(filter, "serialNumber", predicate, Device::getSerialNumber);
        predicate = addPropertyListFilterIfAvailable(filter, "deviceTypeId", predicate, d -> d.getDeviceType()
                .getId());
        predicate = addPropertyListFilterIfAvailable(filter, "deviceConfigurationId", predicate, d -> d.getDeviceConfiguration()
                .getId());
        return predicate;
    }

    private Predicate<Device> addPropertyStringFilterIfAvailabale(JsonQueryFilter filter, String name, Predicate<Device> predicate, Function<Device, String> extractor) {
        Pattern filterPattern = getFilterPattern(filter.getString(name));
        if (filterPattern != null) {
            return predicate.and(d -> {
                String stringToSearch = extractor.apply(d);
                if (stringToSearch == null) {
                    stringToSearch = "";
                }
                return filterPattern.matcher(stringToSearch).matches();
            });
        }
        return predicate;
    }

    private Predicate<Device> addPropertyListFilterIfAvailable(JsonQueryFilter filter, String name, Predicate<Device> predicate, Function<Device, Long> extractor) {
        if (filter.hasProperty(name)) {
            List<Long> list = filter.getLongList(name);
            if (list != null) {
                return predicate.and(d -> list.contains(extractor.apply(d)));
            }
        }
        return predicate;
    }

    /**
     * <ul>
     * <li>Filter a device on the full name (e.g. 123456789)</li>
     * <li>Filter a device ending with a certain set of characters (e.g. *6789)</li>
     * <li>Filter a device beginning with a certain set of characters (e.g. 1234*)</li>
     * <li>Filter a device containing a certain set of characters (e.g. *456*) - Not needed, but we implemented it</li>
     * </ul>
     * Any of he following characters can be used as a wildcard: '*', '?', '%'<br />
     * How do they behave:
     * <ul>
     * <li>A '?' in the pattern matches exactly one character in the value.</li>
     * <li>A percent sign (%) or asterix sign ( * ) in the pattern can match zero or more characters. The pattern '%' or '*' cannot match a null.</li>
     * </ul>
     * See <a href='http://confluence.eict.vpdc/display/JUP/Filter+communication+topology+on+MRID'>Filter communication topology on MRID</a>
     *
     * @param filter the filter expression
     * @return search pattern
     */
    private Pattern getFilterPattern(String filter) {
        if (filter != null) {
            filter = Pattern.quote(filter.replace('%', '*'));
            return Pattern.compile(filter.replaceAll("([*?])", "\\\\E\\.$1\\\\Q"));
        }
        return null;
    }

    @GET
    @Transactional
    @Path("/locations/{locationId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE})
    public Response getLocationAttributes(@PathParam("locationId") long locationId) {
        return Response.ok(locationInfoFactory.from(locationId)).build();
    }


    private boolean needsActivationDate(DeviceMessageId deviceMessageId) {
        return deviceMessageId.equals(ACTIVITY_CALENDER_SEND_WITH_DATE)
                || deviceMessageId.equals(ACTIVITY_CALENDER_SEND_WITH_DATETIME)
                || deviceMessageId.equals(ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_CONTRACT)
                || deviceMessageId.equals(ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_TYPE)
                || deviceMessageId.equals(ACTIVITY_CALENDER_SPECIAL_DAY_CALENDAR_SEND_WITH_CONTRACT_AND_DATETIME);
    }

    private boolean needsType(DeviceMessageId deviceMessageId) {
        return deviceMessageId.equals(ACTIVITY_CALENDAR_SPECIAL_DAY_CALENDAR_SEND_WITH_TYPE)
                || deviceMessageId.equals(ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_TYPE);
    }

    private boolean needsContract(DeviceMessageId deviceMessageId) {
        return deviceMessageId.equals(ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_CONTRACT)
                || deviceMessageId.equals(ACTIVITY_CALENDER_SPECIAL_DAY_CALENDAR_SEND_WITH_CONTRACT_AND_DATETIME);
    }

    private boolean isSpecialDays(DeviceMessageId deviceMessageId) {
        return deviceMessageId.equals(ACTIVITY_CALENDAR_SPECIAL_DAY_CALENDAR_SEND)
                || deviceMessageId.equals(ACTIVITY_CALENDER_SPECIAL_DAY_CALENDAR_SEND_WITH_CONTRACT_AND_DATETIME)
                || deviceMessageId.equals(ACTIVITY_CALENDAR_SPECIAL_DAY_CALENDAR_SEND_WITH_TYPE);
    }
}
