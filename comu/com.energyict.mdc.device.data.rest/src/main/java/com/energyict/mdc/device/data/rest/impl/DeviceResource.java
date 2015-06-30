package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceMessageEnablement;
import com.energyict.mdc.device.config.GatewayType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.imp.DeviceImportService;
import com.energyict.mdc.device.data.rest.DeviceInfoFactory;
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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("/devices")
public class DeviceResource {
    private static final int RECENTLY_ADDED_COUNT = 5;

    private final DeviceImportService deviceImportService;
    private final DeviceService deviceService;
    private final TopologyService topologyService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;
    private final IssueService issueService;
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
    private final Thesaurus thesaurus;

    @Inject
    public DeviceResource(
            ResourceHelper resourceHelper,
            ExceptionFactory exceptionFactory,
            DeviceImportService deviceImportService,
            DeviceService deviceService,
            TopologyService topologyService,
            DeviceConfigurationService deviceConfigurationService,
            IssueService issueService,
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
            DeviceAttributesInfoFactory deviceAttributesInfoFactory,
            Thesaurus thesaurus) {
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.deviceImportService = deviceImportService;
        this.deviceService = deviceService;
        this.topologyService = topologyService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.issueService = issueService;
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
        this.thesaurus = thesaurus;
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    public PagedInfoList getAllDevices(@BeanParam JsonQueryParameters queryParameters, @BeanParam StandardParametersBean params,  @Context UriInfo uriInfo) {
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

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADD_DEVICE)
    public DeviceInfo addDevice(DeviceInfo info, @Context SecurityContext securityContext) {
        Optional<DeviceConfiguration> deviceConfiguration = Optional.empty();
        if (info.deviceConfigurationId != null) {
            deviceConfiguration = deviceConfigurationService.findDeviceConfiguration(info.deviceConfigurationId);
        }

        Device newDevice = deviceService.newDevice(deviceConfiguration.orElse(null), info.mRID, info.mRID);
        newDevice.setSerialNumber(info.serialNumber);
        newDevice.setYearOfCertification(Integer.valueOf(info.yearOfCertification));
        newDevice.save();

        //TODO: Device Date should go on the device wharehouse (future development) - or to go on Batch - creation date

        this.deviceImportService.addDeviceToBatch(newDevice, info.batch);
        return deviceInfoFactory.from(newDevice, getSlaveDevicesForDevice(newDevice));
    }

    @PUT//the method designed like 'PATCH'
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_COMMUNICATION)
    public DeviceInfo updateDevice(@PathParam("id") long id, DeviceInfo info, @Context SecurityContext securityContext) {
        Device device = deviceService.findAndLockDeviceByIdAndVersion(id, info.version).orElseThrow(() -> new WebApplicationException(Status.CONFLICT));
        updateGateway(info, device);
        if (info.estimationStatus != null) {
            updateEstimationStatus(info.estimationStatus, device);
        }
        return DeviceInfo.from(device, getSlaveDevicesForDevice(device), deviceImportService, topologyService, issueService, thesaurus);
    }

    private DeviceInfo updateGateway(DeviceInfo info, Device device) {
        if (info.masterDevicemRID != null) {
            updateGateway(device, info.masterDevicemRID);
        } else {
            removeGateway(device);
        }
        return deviceInfoFactory.from(device, getSlaveDevicesForDevice(device));
    }

    private void updateEstimationStatus(DeviceEstimationStatusInfo info, Device device) {
        if (info.active) {
            device.forEstimation().activateEstimation();
        } else {
            device.forEstimation().deactivateEstimation();
        }
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

    @DELETE
    @Path("/{mRID}")
    @RolesAllowed(Privileges.REMOVE_DEVICE)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response deleteDevice(@PathParam("mRID") String id) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(id);
        device.delete();
        return Response.ok().build();
    }

    @GET
    @Path("/{mRID}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    public DeviceInfo findDeviceTypeBymRID(@PathParam("mRID") String id, @Context SecurityContext securityContext) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(id);
        return deviceInfoFactory.from(device, getSlaveDevicesForDevice(device));
    }

    @GET
    @Path("/{mRID}/attributes")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE})
    public Response getDeviceAttributes(@PathParam("mRID") String id) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(id);
        return Response.ok(deviceAttributesInfoFactory.from(device)).build();
    }

    @PUT
    @Path("/{mRID}/attributes")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.ADMINISTRATE_DEVICE_ATTRIBUTE})
    public Response editDeviceAttributes(@PathParam("mRID") String id, DeviceAttributesInfo info) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(id);
        resourceHelper.findDeviceAndLock(device.getId(), info.deviceVersion);
        deviceAttributesInfoFactory.validateOn(device, info);
        deviceAttributesInfoFactory.writeTo(device, info);
        return Response.ok(deviceAttributesInfoFactory.from(device)).build();
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
    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Path("/{mRID}/messagecategories")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA,
            com.energyict.mdc.device.config.security.Privileges.EXECUTE_DEVICE_MESSAGE_1,
            com.energyict.mdc.device.config.security.Privileges.EXECUTE_DEVICE_MESSAGE_2,
            com.energyict.mdc.device.config.security.Privileges.EXECUTE_DEVICE_MESSAGE_3,
            com.energyict.mdc.device.config.security.Privileges.EXECUTE_DEVICE_MESSAGE_4})
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

    @GET
    @Path("/{mRID}/topology/communication")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA,
            com.energyict.mdc.device.config.security.Privileges.EXECUTE_DEVICE_MESSAGE_1,
            com.energyict.mdc.device.config.security.Privileges.EXECUTE_DEVICE_MESSAGE_2,
            com.energyict.mdc.device.config.security.Privileges.EXECUTE_DEVICE_MESSAGE_3,
            com.energyict.mdc.device.config.security.Privileges.EXECUTE_DEVICE_MESSAGE_4})    public PagedInfoList getCommunicationReferences(@PathParam("mRID") String id, @BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter filter) {
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
