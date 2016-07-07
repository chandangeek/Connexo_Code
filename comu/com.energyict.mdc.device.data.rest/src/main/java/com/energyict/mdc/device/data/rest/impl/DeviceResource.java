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
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceTypePurpose;
import com.energyict.mdc.device.config.GatewayType;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.DevicesForConfigChangeSearch;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.exceptions.CannotChangeDeviceConfigStillUnresolvedConflicts;
import com.energyict.mdc.device.data.rest.DevicePrivileges;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.TopologyTimeline;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.Checks.is;

@Path("/devices")
public class DeviceResource {
    private static final int RECENTLY_ADDED_COUNT = 5;

    private final DeviceService deviceService;
    private final TopologyService topologyService;
    private final DeviceConfigurationService deviceConfigurationService;
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
    private final Clock clock;

    @Inject
    public DeviceResource(
            ResourceHelper resourceHelper,
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
            CalendarService calendarService, Clock clock) {
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
        this.clock = clock;
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
        Device newDevice = newDevice(info.deviceConfigurationId, info.batch, info.mRID, info.serialNumber, info.yearOfCertification, info.shipmentDate);
        return Response.status(Response.Status.CREATED).entity(deviceInfoFactory.from(newDevice, getSlaveDevicesForDevice(newDevice))).build();
    }

    private Device newDevice(long deviceConfigurationId, String batch, String mRID, String serialNumber, int yearOfCertification, Instant shipmentDate) {
        Optional<DeviceConfiguration> deviceConfiguration = deviceConfigurationService.findDeviceConfiguration(deviceConfigurationId);
        Device newDevice;
        if (!is(batch).emptyOrOnlyWhiteSpace()) {
            newDevice = deviceService.newDevice(deviceConfiguration.orElse(null), mRID, mRID, batch, shipmentDate);
        } else {
            newDevice = deviceService.newDevice(deviceConfiguration.orElse(null), mRID, mRID, shipmentDate);
        }
        newDevice.setSerialNumber(serialNumber);
        newDevice.setYearOfCertification(yearOfCertification);
        newDevice.save();
        newDevice.getLifecycleDates().setReceivedDate(shipmentDate).save();
        return newDevice;
    }

    @PUT @Transactional
    @Path("/changedeviceconfig")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION)
    public Response changeDeviceConfig(BulkRequestInfo request, @BeanParam JsonQueryFilter queryFilter, @Context SecurityContext securityContext) {
        if (request.action == null || (!request.action.equalsIgnoreCase("ChangeDeviceConfiguration"))) {
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
                request.deviceMRIDs.toArray(new String[request.deviceMRIDs.size()]));
        return Response.ok().build();
    }

    @PUT//the method designed like 'PATCH'
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION)
    public Response updateDevice(@PathParam("id") long id, DeviceInfo info) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(info.mRID);
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
        if (info.masterDevicemRID != null) {
            updateGateway(device, info.masterDevicemRID);
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
                validateBeforeCreatingNewSlaveViaWizard(slaveDeviceInfo.mRID);
                slave = newDevice(slaveDeviceInfo.deviceConfigurationId, slaveDeviceInfo.batch, slaveDeviceInfo.mRID, slaveDeviceInfo.serialNumber, slaveDeviceInfo.yearOfCertification, Instant
                        .ofEpochMilli(slaveDeviceInfo.shipmentDate));
            } else {
                if (slaveDeviceInfo.isFromExistingLink()) {
                    // No new link, came along with deviceinfo
                    return;
                }
                slave = deviceService.findByUniqueMrid(slaveDeviceInfo.mRID)
                        .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_DEVICE, slaveDeviceInfo.mRID));
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
     * Validates the uniqueness of the mrid when creating a datalogger slave via the wizard.
     * We do the validation here because it doesn't properly work with form-validation
     *
     * @param mRID the mrid of the new-to-create datalogger slave
     */
    private void validateBeforeCreatingNewSlaveViaWizard(String mRID) {
        Optional<Device> existingDevice = deviceService.findByUniqueMrid(mRID);
        if (existingDevice.isPresent()) {
            throw exceptionFactory.newException(MessageSeeds.UNIQUE_MRID);
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
        return resourceHelper.findRegisterOnDeviceOrThrowException(info.mRID, info.id );
    }

    private Register registerInfoToRegister(Device slave, RegisterInfo info){
        return resourceHelper.findRegisterOnDeviceOrThrowException(slave, info.id );
    }

    private void updateGateway(Device device, String gatewayMRID) {
        if (device.getDeviceConfiguration().isDirectlyAddressable()) {
            throw exceptionFactory.newException(MessageSeeds.IMPOSSIBLE_TO_SET_MASTER_DEVICE, device.getmRID());
        }
        Optional<Device> currentGateway = topologyService.getPhysicalGateway(device);
        if (!currentGateway.isPresent() || !currentGateway.get().getmRID().equals(gatewayMRID)) {
            Device newGateway = resourceHelper.findDeviceByMrIdOrThrowException(gatewayMRID);
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
            slaves = DeviceTopologyInfo.from(topologyService.getPhysicalTopologyTimelineAdditions(device, RECENTLY_ADDED_COUNT), resourceHelper.getThesaurus());
        } else {
            slaves = DeviceTopologyInfo.from(topologyService.getPysicalTopologyTimeline(device), resourceHelper.getThesaurus());
        }
        return slaves;
    }

    @GET @Transactional
    @Path("/{mRID}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public DeviceInfo findDeviceBymRID(@PathParam("mRID") String mrid, @Context SecurityContext securityContext) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        return deviceInfoFactory.from(device, getSlaveDevicesForDevice(device));
    }

    @GET @Transactional
    @Path("/{mRID}/attributes")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE})
    public Response getDeviceAttributes(@PathParam("mRID") String id) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(id);
        return Response.ok(deviceAttributesInfoFactory.from(device)).build();
    }

    @PUT @Transactional
    @Path("/{mRID}/attributes")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_ATTRIBUTE})
    public Response editDeviceAttributes(@PathParam("mRID") String id, DeviceAttributesInfo info) {
        Device device = resourceHelper.lockDeviceOrThrowException(info.device);
        deviceAttributesInfoFactory.validateOn(device, info);
        deviceAttributesInfoFactory.writeTo(device, info);
        return Response.ok(deviceAttributesInfoFactory.from(device)).build();
    }

    @GET @Transactional
    @Path("/{mRID}/customproperties")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE})
    public PagedInfoList getDeviceCustomProperties(@PathParam("mRID") String mRID, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        List<CustomPropertySetInfo> customPropertySetInfos = resourceHelper.getDeviceCustomPropertySetInfos(device);
        return PagedInfoList.fromCompleteList("customproperties", customPropertySetInfos, queryParameters);
    }

    @GET @Transactional
    @Path("/{mRID}/customproperties/{cpsId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE})
    public CustomPropertySetInfo getDeviceCustomProperty(@PathParam("mRID") String mRID, @PathParam("cpsId") long cpsId) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        return resourceHelper.getDeviceCustomPropertySetInfos(device)
                .stream()
                .filter(f -> f.id == cpsId)
                .findFirst()
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_CUSTOMPROPERTYSET, cpsId));
    }

    @GET @Transactional
    @Path("/{mRID}/customproperties/{cpsId}/versions")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE})
    public PagedInfoList getDeviceCustomPropertyVersioned(@PathParam("mRID") String mRID, @PathParam("cpsId") long cpsId, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        List<CustomPropertySetInfo> customPropertySetInfoList = resourceHelper.getVersionedCustomPropertySetHistoryInfos(device, cpsId);
        return PagedInfoList.fromCompleteList("versions", customPropertySetInfoList, queryParameters);
    }

    @GET @Transactional
    @Path("/{mRID}/customproperties/{cpsId}/versions/{timeStamp}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE})
    public CustomPropertySetInfo getDeviceCustomPropertyVersionedHistory(@PathParam("mRID") String mRID, @PathParam("cpsId") long cpsId, @PathParam("timeStamp") Long timeStamp) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        return resourceHelper.getDeviceCustomPropertySetInfos(device, Instant.ofEpochMilli(timeStamp))
                .stream()
                .filter(f -> f.id == cpsId)
                .findFirst()
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_CUSTOMPROPERTYSET, cpsId));
    }

    @GET @Transactional
    @Path("/{mRID}/customproperties/{cpsId}/currentinterval")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public IntervalInfo getCurrentTimeInterval(@PathParam("mRID") String mRID, @PathParam("cpsId") long cpsId) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        Interval interval = Interval.of(resourceHelper.getCurrentTimeInterval(device, cpsId));

        return IntervalInfo.from(interval.toClosedOpenRange());
    }

    @GET @Transactional
    @Path("/{mRID}/customproperties/{cpsId}/conflicts")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public PagedInfoList getOverlaps(@PathParam("mRID") String mRID, @PathParam("cpsId") long cpsId, @QueryParam("startTime") long startTime, @QueryParam("endTime") long endTime, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        List<CustomPropertySetIntervalConflictInfo> overlapInfos = resourceHelper.getOverlapsWhenCreate(device, cpsId, resourceHelper.getTimeRange(startTime, endTime));
        Collections.sort(overlapInfos, resourceHelper.getConflictInfosComparator());
        return PagedInfoList.fromCompleteList("conflicts", overlapInfos, queryParameters);
    }

    @GET @Transactional
    @Path("/{mRID}/customproperties/{cpsId}/conflicts/{timeStamp}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public PagedInfoList getOverlaps(@PathParam("mRID") String mRID, @PathParam("cpsId") long cpsId, @PathParam("timeStamp") long timeStamp, @QueryParam("startTime") long startTime, @QueryParam("endTime") long endTime, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        List<CustomPropertySetIntervalConflictInfo> overlapInfos = resourceHelper.getOverlapsWhenUpdate(device, cpsId, resourceHelper.getTimeRange(startTime, endTime), Instant.ofEpochMilli(timeStamp));
        Collections.sort(overlapInfos, resourceHelper.getConflictInfosComparator());
        return PagedInfoList.fromCompleteList("conflicts", overlapInfos, queryParameters);
    }

    @PUT @Transactional
    @Path("/{mRID}/customproperties/{cpsId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public Response editDeviceCustomAttribute(@PathParam("mRID") String mRID, @PathParam("cpsId") long cpsId, CustomPropertySetInfo customPropertySetInfo) {
        Device lockedDevice = resourceHelper.lockDeviceOrThrowException(customPropertySetInfo.parent, mRID, customPropertySetInfo.version);
        resourceHelper.lockDeviceTypeOrThrowException(customPropertySetInfo.objectTypeId, customPropertySetInfo.objectTypeVersion);
        resourceHelper.setDeviceCustomPropertySetInfo(lockedDevice, cpsId, customPropertySetInfo);
        return Response.ok().build();
    }

    @POST @Transactional
    @Path("/{mRID}/customproperties/{cpsId}/versions")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA, Privileges.Constants.ADMINISTER_DEVICE_TIME_SLICED_CPS})
    public Response addDeviceCustomAttributeVersioned(@PathParam("mRID") String mRID, @PathParam("cpsId") long cpsId, @QueryParam("forced") boolean forced, CustomPropertySetInfo customPropertySetInfo) {
        Device lockedDevice = resourceHelper.lockDeviceOrThrowException(customPropertySetInfo.parent, mRID, customPropertySetInfo.version);
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
    @Path("/{mRID}/customproperties/{cpsId}/versions/{timeStamp}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA, Privileges.Constants.ADMINISTER_DEVICE_TIME_SLICED_CPS})
    public Response editDeviceCustomAttributeVersioned(@PathParam("mRID") String mRID, @PathParam("cpsId") long cpsId, @PathParam("timeStamp") long timeStamp, @QueryParam("forced") boolean forced, CustomPropertySetInfo customPropertySetInfo) {
        Device lockedDevice = resourceHelper.lockDeviceOrThrowException(customPropertySetInfo.parent, mRID, customPropertySetInfo.version);
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
    @Path("/{mRID}/privileges")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE})
    public Response getDeviceConstraintsBasedOnDeviceState(@PathParam("mRID") String id, @BeanParam JsonQueryParameters queryParameters, @Context SecurityContext securityContext) {
        User user = (User) securityContext.getUserPrincipal();
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(id);
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
     * @param mrid Device's mRID
     * @return List of categories + device message specs, indicating if message spec will be picked up by a comtask or not
     */
    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{mRID}/messagecategories")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA,
            com.energyict.mdc.device.config.security.Privileges.Constants.EXECUTE_DEVICE_MESSAGE_1,
            com.energyict.mdc.device.config.security.Privileges.Constants.EXECUTE_DEVICE_MESSAGE_2,
            com.energyict.mdc.device.config.security.Privileges.Constants.EXECUTE_DEVICE_MESSAGE_3,
            com.energyict.mdc.device.config.security.Privileges.Constants.EXECUTE_DEVICE_MESSAGE_4})
    public PagedInfoList getAllAvailableDeviceCategoriesIncludingMessageSpecsForCurrentUser(@PathParam("mRID") String mrid, @BeanParam JsonQueryParameters queryParameters, @Context UriInfo uriInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
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

    @Path("/{mRID}/connectionmethods")
    public ConnectionMethodResource getConnectionMethodResource() {
        return connectionMethodResourceProvider.get();
    }

    @Path("/{mRID}/protocoldialects")
    public ProtocolDialectResource getProtocolDialectsResource() {
        return protocolDialectResourceProvider.get();
    }

    @Path("/{mRID}/registers")
    public RegisterResource getRegisterResource() {
        return registerResourceProvider.get();
    }

    @Path("/{mRID}/channels")
    public ChannelResource getChannelResource() {
        return channelsOnDeviceResourceProvider.get();
    }

    @Path("/{mRID}/validationrulesets")
    public DeviceValidationResource getDeviceValidationResource() {
        return deviceValidationResourceProvider.get();
    }

    @Path("/{mRID}/estimationrulesets")
    public DeviceEstimationResource getDeviceEstimationResource() {
        return deviceEstimationResourceProvider.get();
    }

    @Path("/{mRID}/loadprofiles")
    public LoadProfileResource getLoadProfileResource() {
        return loadProfileResourceProvider.get();
    }

    @Path("/{mRID}/logbooks")
    public LogBookResource getLogBookResource() {
        return logBookResourceProvider.get();
    }

    @Path("/{mRID}/whatsgoingon")
    public GoingOnResource getGoingOnResource() {
        return goingOnResourceProvider.get();
    }

    @Path("/schedules")
    public BulkScheduleResource getBulkScheduleResource() {
        return bulkScheduleResourceProvider.get();
    }

    @Path("/{mRID}/schedules")
    public DeviceScheduleResource getComTaskExecutionResource() {
        return deviceScheduleResourceProvider.get();
    }

    @Path("/{mRID}/comtasks")
    public DeviceComTaskResource getComTaskResource() {
        return deviceComTaskResourceProvider.get();
    }

    @Path("/{mRID}/devicemessages")
    public DeviceMessageResource getCommandResource() {
        return deviceCommandResourceProvider.get();
    }

    @Path("/{mRID}/securityproperties")
    public SecurityPropertySetResource getSecurityPropertySetResource() {
        return securityPropertySetResourceProvider.get();
    }

    @Path("/{mRID}/protocols")
    public DeviceProtocolPropertyResource getDevicePropertyResource(@PathParam("mRID") String mRID) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        return devicePropertyResourceProvider.get().with(device);
    }

    @Path("/{mRID}/devicelabels")
    public DeviceLabelResource getDeviceLabelResource() {
        return deviceLabelResourceProvider.get();
    }

    @Path("/{mRID}/connections")
    public ConnectionResource getConnectionResource() {
        return connectionResourceProvider.get();
    }

    @Path("/{mRID}/history")
    public DeviceHistoryResource getDeviceHistoryResource() {
        return deviceHistoryResourceProvider.get();
    }

    @Path("/{mRID}/transitions")
    public DeviceLifeCycleActionResource getDeviceLifeCycleActionsResource() {
        return deviceLifeCycleActionResourceProvider.get();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("{mRID}/runningservicecalls")
    public PagedInfoList getServiceCallsFor(@PathParam("mRID") String mrid, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        List<ServiceCallInfo> serviceCallInfos = new ArrayList<>();
        Set<DefaultState> states = EnumSet.of(
                DefaultState.CREATED,
                DefaultState.SCHEDULED,
                DefaultState.PENDING,
                DefaultState.PAUSED,
                DefaultState.ONGOING,
                DefaultState.WAITING);

        serviceCallService.findServiceCalls(device, states)
                .stream()
                .forEach(serviceCall -> serviceCallInfos.add(serviceCallInfoFactory.summarized(serviceCall)));

        return PagedInfoList.fromCompleteList("serviceCalls", serviceCallInfos, queryParameters);
    }

    @PUT
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("{mRID}/runningservicecalls/{id}")
    public Response cancelServiceCall(@PathParam("mRID") String mrid, @PathParam("id") long serviceCallId, ServiceCallInfo info) {
        if (info.state.id.equals("sclc.default.cancelled")) {
            serviceCallService.getServiceCall(serviceCallId).ifPresent(ServiceCall::cancel);
            return Response.status(Response.Status.ACCEPTED).build();
        }
        throw exceptionFactory.newException(MessageSeeds.BAD_REQUEST);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("{mRID}/servicecallhistory")
    public PagedInfoList getServiceCallHistoryFor(@PathParam("mRID") String mrid, @BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter jsonQueryFilter) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
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
    @Path("/{mRID}/servicecalls")
    public Response cancelServiceCallsFor(@PathParam("mRID") String mrid, ServiceCallInfo serviceCallInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
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
    @Path("/{mRID}/topology/communication")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA,
            com.energyict.mdc.device.config.security.Privileges.Constants.EXECUTE_DEVICE_MESSAGE_1,
            com.energyict.mdc.device.config.security.Privileges.Constants.EXECUTE_DEVICE_MESSAGE_2,
            com.energyict.mdc.device.config.security.Privileges.Constants.EXECUTE_DEVICE_MESSAGE_3,
            com.energyict.mdc.device.config.security.Privileges.Constants.EXECUTE_DEVICE_MESSAGE_4})
    public PagedInfoList getCommunicationReferences(@PathParam("mRID") String id, @BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter filter) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(id);
        Integer limit = queryParameters.getLimit().orElse(Integer.MAX_VALUE);
        TopologyTimeline timeline = topologyService.getPhysicalTopologyTimelineAdditions(device, limit);
        Predicate<Device> filterPredicate = getFilterForCommunicationTopology(filter);
        Stream<Device> stream = timeline.getAllDevices().stream().filter(filterPredicate)
                .sorted(Comparator.comparing(Device::getmRID));
        if (queryParameters.getStart().isPresent() && queryParameters.getStart().get() > 0) {
            stream = stream.skip(queryParameters.getStart().get());
        }
        List<DeviceTopologyInfo> topologyList = stream.map(d -> DeviceTopologyInfo.from(d, timeline.mostRecentlyAddedOn(d), resourceHelper.getThesaurus()))
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("slaveDevices", topologyList, queryParameters);
    }

    @GET @Transactional
    @Path("/{mRID}/dataloggerslaves")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getDataLoggerSlaves(@PathParam("mRID") String mrid, @BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter filter) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        return PagedInfoList.fromPagedList("dataLoggerSlaveDevices", getDataLoggerSlavesForDevice(device), queryParameters);
    }

    private List<DeviceTopologyInfo> getDataLoggerSlavesForDevice(Device device) {
        return (device.getDeviceConfiguration().isDataloggerEnabled() ? resourceHelper.getDataLoggerSlaves(device): Collections.emptyList());
    }

    @GET @Transactional
    @Path("/unlinkeddataloggerslaves")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public DeviceInfos getUnlinkedSlaves(@BeanParam JsonQueryParameters queryParameters) {
        String searchText = queryParameters.getLike();
        if (searchText != null && !searchText.isEmpty()) {
            return new DeviceInfos(
                deviceService.findAllDevices(getUnlinkedSlaveDevicesCondition(searchText)).stream()
                .limit(50)
                .collect(Collectors.<Device>toList())
            );
        }
        return new DeviceInfos();
    }

    private Condition getUnlinkedSlaveDevicesCondition(String dbSearchText) {
        // a. Datalogger slave devices
        String regex = "*".concat(dbSearchText.replace(" ", "*").concat("*"));
        Condition a = Where.where("mRID").likeIgnoreCase(regex)
            .and(Where.where("deviceType.deviceTypePurpose").isEqualTo(DeviceTypePurpose.DATALOGGER_SLAVE));
        // b. that are not linked yet to a data logger
        Condition b = ListOperator.NOT_IN.contains(topologyService.findAllEffectiveDataLoggerSlaveDevices().asSubQuery("origin"), "id");
        return a.and(b);
    }

    @GET
    @Transactional
    @Path("/{mRID}/timeofuse")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.VIEW_DEVICE)
    public Response getCalendarInfo(@PathParam("mRID") String id) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(id);
        TimeOfUseInfo info = timeOfUseInfoFactory.from(device.getActiveCalendar(), device.getPassiveCalendars(), device, calendarInfoFactory);

        return Response.ok(info).build();
    }

    @GET
    @Path("/{mRID}/timeofuse/{calendarId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.VIEW_DEVICE)
    public Response getCalendar(@PathParam("id") long id, @PathParam("calendarId") long calendarId, @QueryParam("weekOf") long milliseconds) {
        if(milliseconds <= 0) {
            return  Response.ok(calendarService.findCalendar(calendarId)
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

    private CalendarInfo transformToWeekCalendar(Calendar calendar, LocalDate localDate) {
        return calendarInfoFactory.detailedWeekFromCalendar(calendar, localDate);
    }

    private Predicate<Device> getFilterForCommunicationTopology(JsonQueryFilter filter) {
        Predicate<Device> predicate = d -> true;
        predicate = addPropertyStringFilterIfAvailabale(filter, "mrid", predicate, Device::getmRID);
        predicate = addPropertyStringFilterIfAvailabale(filter, "serialNumber", predicate, Device::getSerialNumber);
        predicate = addPropertyListFilterIfAvailabale(filter, "deviceTypeId", predicate, d -> d.getDeviceType().getId());
        predicate = addPropertyListFilterIfAvailabale(filter, "deviceConfigurationId", predicate, d -> d.getDeviceConfiguration().getId());
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

    private Predicate<Device> addPropertyListFilterIfAvailabale(JsonQueryFilter filter, String name, Predicate<Device> predicate, Function<Device, Long> extractor) {
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
     * <li>Filter a device on the full MRID (e.g. 123456789)</li>
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
}