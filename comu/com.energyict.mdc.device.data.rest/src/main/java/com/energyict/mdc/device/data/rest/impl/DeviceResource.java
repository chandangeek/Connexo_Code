package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.common.rest.IntervalInfo;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceMessageEnablement;
import com.energyict.mdc.device.config.GatewayType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.rest.DeviceInfoFactory;
import com.energyict.mdc.device.data.rest.DevicePrivileges;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.TopologyTimeline;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

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
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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
    private final DeviceInfoFactory deviceInfoFactory;
    private final DeviceAttributesInfoFactory deviceAttributesInfoFactory;

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
            DeviceInfoFactory deviceInfoFactory,
            DeviceAttributesInfoFactory deviceAttributesInfoFactory) {
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
        this.deviceInfoFactory = deviceInfoFactory;
        this.deviceAttributesInfoFactory = deviceAttributesInfoFactory;
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
        List<DeviceInfo> deviceInfos = deviceInfoFactory.from(allDevices); //DeviceInfo.from(allDevices);
        return PagedInfoList.fromPagedList("devices", deviceInfos, queryParameters);
    }

    @POST @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADD_DEVICE)
    public DeviceInfo addDevice(DeviceInfo info, @Context SecurityContext securityContext) {
        Optional<DeviceConfiguration> deviceConfiguration = deviceConfigurationService.findDeviceConfiguration(info.deviceConfigurationId);
        Device newDevice;
        if (!is(info.batch).emptyOrOnlyWhiteSpace()) {
            newDevice = deviceService.newDevice(deviceConfiguration.orElse(null), info.mRID, info.mRID, info.batch);
        } else {
            newDevice = deviceService.newDevice(deviceConfiguration.orElse(null), info.mRID, info.mRID);
        }
        newDevice.setSerialNumber(info.serialNumber);
        newDevice.setYearOfCertification(info.yearOfCertification);
        newDevice.save();

        //TODO: Device Date should go on the device wharehouse (future development) - or to go on Batch - creation date
        return deviceInfoFactory.from(newDevice, getSlaveDevicesForDevice(newDevice));
    }

    @PUT @Transactional//the method designed like 'PATCH'
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION)
    public DeviceInfo updateDevice(@PathParam("id") long id, DeviceInfo info) {
        Device device = resourceHelper.lockDeviceOrThrowException(info);
        updateGateway(info, device);
        device.save();
        return deviceInfoFactory.from(device, getSlaveDevicesForDevice(device));
    }

    private DeviceInfo updateGateway(DeviceInfo info, Device device) {
        if (info.masterDevicemRID != null) {
            updateGateway(device, info.masterDevicemRID);
        } else {
            removeGateway(device);
        }
        return deviceInfoFactory.from(device, getSlaveDevicesForDevice(device));
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
        if (topologyService.getPhysicalGateway(device).isPresent()) {
            topologyService.clearPhysicalGateway(device);
        }
    }

    private List<DeviceTopologyInfo> getSlaveDevicesForDevice(Device device) {
        List<DeviceTopologyInfo> slaves;
        if (GatewayType.LOCAL_AREA_NETWORK.equals(device.getConfigurationGatewayType())) {
            slaves = DeviceTopologyInfo.from(topologyService.getPhysicalTopologyTimelineAdditions(device, RECENTLY_ADDED_COUNT));
        } else {
            slaves = DeviceTopologyInfo.from(topologyService.findPhysicalConnectedDevices(device));
        }
        return slaves;
    }

    @GET @Transactional
    @Path("/{mRID}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public DeviceInfo findDeviceTypeBymRID(@PathParam("mRID") String id, @Context SecurityContext securityContext) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(id);
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
        CustomPropertySetInfo customPropertySetInfo = resourceHelper.getDeviceCustomPropertySetInfos(device)
                .stream()
                .filter(f -> f.id == cpsId)
                .findFirst()
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_CUSTOMPROPERTYSET, cpsId));
        return customPropertySetInfo;
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
        CustomPropertySetInfo customPropertySetInfo = resourceHelper.getDeviceCustomPropertySetInfos(device, Instant.ofEpochMilli(timeStamp))
                .stream()
                .filter(f -> f.id == cpsId)
                .findFirst()
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_CUSTOMPROPERTYSET, cpsId));
        return customPropertySetInfo;
    }

    @GET @Transactional
    @Path("/{mRID}/customproperties/{cpsId}/currentinterval")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public IntervalInfo getCurrentTimeInterval(@PathParam("mRID") String mRID, @PathParam("cpsId") long cpsId) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        Interval interval = Interval.of(resourceHelper.getCurrentTimeInterval(device, cpsId));

        return IntervalInfo.from(interval.toClosedOpenRange());
    }

    @GET @Transactional
    @Path("/{mRID}/customproperties/{cpsId}/conflicts")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getOverlaps(@PathParam("mRID") String mRID, @PathParam("cpsId") long cpsId, @QueryParam("startTime") long startTime, @QueryParam("endTime") long endTime, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        List<CustomPropertySetIntervalConflictInfo> overlapInfos = resourceHelper.getOverlapsWhenCreate(device, cpsId, startTime, endTime);
        if (!overlapInfos.isEmpty()) {
            CustomPropertySetInfo insertedValuesStub = new CustomPropertySetInfo();
            insertedValuesStub.startTime = startTime;
            insertedValuesStub.versionId = startTime;
            insertedValuesStub.endTime = endTime;
            overlapInfos.add(new CustomPropertySetIntervalConflictInfo(MessageSeeds.CUSTOMPROPRTTYSET_TIMESLICED_INSERT, insertedValuesStub, true));
            Collections.sort(overlapInfos, resourceHelper.getConflictInfosComparator());
        }
        return PagedInfoList.fromCompleteList("conflicts", overlapInfos, queryParameters);
    }

    @GET @Transactional
    @Path("/{mRID}/customproperties/{cpsId}/conflicts/{timeStamp}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getOverlaps(@PathParam("mRID") String mRID, @PathParam("cpsId") long cpsId, @PathParam("timeStamp") long timeStamp, @QueryParam("startTime") long startTime, @QueryParam("endTime") long endTime, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        List<CustomPropertySetIntervalConflictInfo> overlapInfos = resourceHelper.getOverlapsWhenUpdate(device, cpsId, startTime, endTime, Instant.ofEpochMilli(timeStamp));
        if (!overlapInfos.isEmpty()) {
            CustomPropertySetInfo insertedValuesStub = new CustomPropertySetInfo();
            insertedValuesStub.startTime = startTime;
            insertedValuesStub.versionId = startTime;
            insertedValuesStub.endTime = endTime;
            overlapInfos.add(new CustomPropertySetIntervalConflictInfo(MessageSeeds.CUSTOMPROPRTTYSET_TIMESLICED_INSERT, insertedValuesStub, true));
            Collections.sort(overlapInfos, resourceHelper.getConflictInfosComparator());
        }
        return PagedInfoList.fromCompleteList("conflicts", overlapInfos, queryParameters);
    }

    @PUT @Transactional
    @Path("/{mRID}/customproperties/{cpsId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public Response editDeviceCustomAttribute(@PathParam("mRID") String mRID, @PathParam("cpsId") long cpsId, CustomPropertySetInfo customPropertySetInfo) {
        Device lockedDevice = resourceHelper.lockDeviceOrThrowException(customPropertySetInfo.parent, mRID, customPropertySetInfo.version);
        resourceHelper.setDeviceCustomPropertySetInfo(lockedDevice, cpsId, customPropertySetInfo);
        return Response.ok().build();
    }

    @POST @Transactional
    @Path("/{mRID}/customproperties/{cpsId}/versions")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public Response addDeviceCustomAttributeVersioned(@PathParam("mRID") String mRID, @PathParam("cpsId") long cpsId, @QueryParam("forced") boolean forced, CustomPropertySetInfo customPropertySetInfo) {
        Device lockedDevice = resourceHelper.lockDeviceOrThrowException(customPropertySetInfo.parent, mRID, customPropertySetInfo.version);
        Optional<IntervalErrorInfos> intervalErrors = resourceHelper.verifyTimeRange(customPropertySetInfo.startTime, customPropertySetInfo.endTime);
        if (intervalErrors.isPresent()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(intervalErrors.get()).build();
        }
        resourceHelper.addDeviceCustomPropertySetVersioned(lockedDevice, cpsId, customPropertySetInfo, forced);
        return Response.ok().build();
    }

    @PUT @Transactional
    @Path("/{mRID}/customproperties/{cpsId}/versions/{timeStamp}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public Response editDeviceCustomAttributeVersioned(@PathParam("mRID") String mRID, @PathParam("cpsId") long cpsId, @PathParam("timeStamp") long timeStamp, @QueryParam("forced") boolean forced, CustomPropertySetInfo customPropertySetInfo) {
        Device lockedDevice = resourceHelper.lockDeviceOrThrowException(customPropertySetInfo.parent, mRID, customPropertySetInfo.version);
        Optional<IntervalErrorInfos> intervalErrors = resourceHelper.verifyTimeRange(customPropertySetInfo.startTime, customPropertySetInfo.endTime);
        if (intervalErrors.isPresent()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(intervalErrors.get()).build();
        }
        resourceHelper.setDeviceCustomPropertySetVersioned(lockedDevice, cpsId, customPropertySetInfo, Instant.ofEpochMilli(timeStamp), forced);
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
    public PagedInfoList getAllAvailableDeviceCategoriesIncludingMessageSpecsForCurrentUser(@PathParam("mRID") String mrid, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);

        Set<DeviceMessageId> supportedMessagesSpecs = device.getDeviceType().getDeviceProtocolPluggableClass().getDeviceProtocol().getSupportedMessages();

        List<DeviceMessageId> enabledDeviceMessageIds = device.getDeviceConfiguration().getDeviceMessageEnablements().stream().map(DeviceMessageEnablement::getDeviceMessageId).collect(Collectors.toList());
        List<DeviceMessageCategoryInfo> infos = new ArrayList<>();

        deviceMessageSpecificationService.filteredCategoriesForUserSelection().stream().sorted((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName())).forEach(category -> {
            List<DeviceMessageSpecInfo> deviceMessageSpecs = category.getMessageSpecifications().stream()
                    .filter(deviceMessageSpec -> supportedMessagesSpecs.contains(deviceMessageSpec.getId())) // limit to device message specs supported by the protocol
                    .filter(dms -> enabledDeviceMessageIds.contains(dms.getId())) // limit to device message specs enabled on the config
                    .filter(dms -> device.getDeviceConfiguration().isAuthorized(dms.getId())) // limit to device message specs whom the user is authorized to
                    .sorted((dms1, dms2) -> dms1.getName().compareToIgnoreCase(dms2.getName()))
                    .map(dms -> deviceMessageSpecInfoFactory.asInfoWithMessagePropertySpecs(dms, device))
                    .collect(Collectors.toList());
            if (!deviceMessageSpecs.isEmpty()) {
                DeviceMessageCategoryInfo info = deviceMessageCategoryInfoFactory.asInfo(category);
                info.deviceMessageSpecs = deviceMessageSpecs;
                infos.add(info);
            }
        });
        List<DeviceMessageCategoryInfo> deviceMessageCategoryInfosInPage = ListPager.of(infos).from(queryParameters).find();
        return PagedInfoList.fromPagedList("categories", deviceMessageCategoryInfosInPage, queryParameters);
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
        TopologyTimeline timeline = topologyService.getPysicalTopologyTimeline(device);
        Predicate<Device> filterPredicate = getFilterForCommunicationTopology(filter);
        Stream<Device> stream = timeline.getAllDevices().stream().filter(filterPredicate)
                .sorted(Comparator.comparing(Device::getmRID));
        if (queryParameters.getStart().isPresent() && queryParameters.getStart().get() > 0) {
            stream = stream.skip(queryParameters.getStart().get());
        }
        if (queryParameters.getLimit().isPresent() && queryParameters.getLimit().get() > 0) {
            stream = stream.limit(queryParameters.getLimit().get() + 1);
        }
        List<DeviceTopologyInfo> topologyList = stream.map(d -> DeviceTopologyInfo.from(d, timeline.mostRecentlyAddedOn(d))).collect(Collectors.toList());
        return PagedInfoList.fromPagedList("slaveDevices", topologyList, queryParameters);
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
}