package com.energyict.mdc.device.data.rest.impl;


import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceMessageEnablement;
import com.energyict.mdc.device.config.GatewayType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.topology.TopologyTimeline;
import com.energyict.mdc.device.data.imp.DeviceImportService;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.topology.TopologyService;
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
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
    private final IssueService issueService;
    private final Provider<ProtocolDialectResource> protocolDialectResourceProvider;
    private final Provider<LoadProfileResource> loadProfileResourceProvider;
    private final Provider<LogBookResource> logBookResourceProvider;
    private final Provider<DeviceValidationResource> deviceValidationResourceProvider;
    private final Provider<RegisterResource> registerResourceProvider;
    private final Provider<BulkScheduleResource> bulkScheduleResourceProvider;
    private final Provider<DeviceScheduleResource> deviceScheduleResourceProvider;
    private final Provider<DeviceComTaskResource> deviceComTaskResourceProvider;
    private final Provider<SecurityPropertySetResource> securityPropertySetResourceProvider;
    private final Provider<ConnectionMethodResource> connectionMethodResourceProvider;
    private final Provider<DeviceMessageResource> deviceCommandResourceProvider;
    private final Provider<DeviceLabelResource> deviceLabelResourceProvider;
    private final Provider<ConnectionResource> connectionResourceProvider;
    private final Provider<CommunicationResource> communicationResourceProvider;
    private final DeviceMessageSpecificationService deviceMessageSpecificationService;
    private final DeviceMessageSpecInfoFactory deviceMessageSpecInfoFactory;
    private final DeviceMessageCategoryInfoFactory deviceMessageCategoryInfoFactory;

    @Inject
    public DeviceResource(
            ResourceHelper resourceHelper,
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
            Provider<BulkScheduleResource> bulkScheduleResourceProvider,
            Provider<DeviceScheduleResource> deviceScheduleResourceProvider,
            Provider<DeviceComTaskResource> deviceComTaskResourceProvider,
            Provider<DeviceMessageResource> deviceCommandResourceProvider,
            Provider<ConnectionResource> connectionResourceProvider,
            Provider<CommunicationResource> communicationResourceProvider,
            DeviceMessageSpecificationService deviceMessageSpecificationService,
            DeviceMessageSpecInfoFactory deviceMessageSpecInfoFactory,
            DeviceMessageCategoryInfoFactory deviceMessageCategoryInfoFactory,
            Provider<SecurityPropertySetResource> securityPropertySetResourceProvider,
            Provider<ConnectionMethodResource> connectionMethodResourceProvider,
            Provider<DeviceLabelResource> deviceLabelResourceProvider) {
        this.resourceHelper = resourceHelper;
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
        this.bulkScheduleResourceProvider = bulkScheduleResourceProvider;
        this.deviceScheduleResourceProvider = deviceScheduleResourceProvider;
        this.deviceComTaskResourceProvider = deviceComTaskResourceProvider;
        this.securityPropertySetResourceProvider = securityPropertySetResourceProvider;
        this.connectionMethodResourceProvider = connectionMethodResourceProvider;
        this.deviceCommandResourceProvider = deviceCommandResourceProvider;
        this.deviceLabelResourceProvider = deviceLabelResourceProvider;
        this.connectionResourceProvider = connectionResourceProvider;
        this.communicationResourceProvider = communicationResourceProvider;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        this.deviceMessageSpecInfoFactory = deviceMessageSpecInfoFactory;
        this.deviceMessageCategoryInfoFactory = deviceMessageCategoryInfoFactory;
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE,Privileges.VIEW_DEVICE})
    public PagedInfoList getAllDevices(@BeanParam QueryParameters queryParameters, @BeanParam StandardParametersBean params,  @Context UriInfo uriInfo) {
        Condition condition = null;
        MultivaluedMap<String, String> uriParams = uriInfo.getQueryParameters();
        if (uriParams.containsKey("filter")) {
            condition = resourceHelper.getQueryConditionForDevice(uriInfo.getQueryParameters());
        } else {
            condition = resourceHelper.getQueryConditionForDevice(params);
        }
        Finder<Device> allDevicesFinder = deviceService.findAllDevices(condition);
        List<Device> allDevices = allDevicesFinder.from(queryParameters).find();
        List<DeviceInfo> deviceInfos = DeviceInfo.from(allDevices);
        return PagedInfoList.asJson("devices", deviceInfos, queryParameters);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE)
    public DeviceInfo addDevice(DeviceInfo info, @Context SecurityContext securityContext) {
        Optional<DeviceConfiguration> deviceConfiguration = Optional.empty();
        if (info.deviceConfigurationId != null) {
            deviceConfiguration = deviceConfigurationService.findDeviceConfiguration(info.deviceConfigurationId);
        }

        Device newDevice = deviceService.newDevice(deviceConfiguration.orElse(null), info.mRID, info.mRID);
        newDevice.setSerialNumber(info.serialNumber);
        newDevice.setYearOfCertification(ZonedDateTime.of(Integer.parseInt(info.yearOfCertification), 1, 1, 0, 0, 0, 0, ZoneId.of("UTC")).toInstant());
        newDevice.save();

        //TODO: Device Date should go on the device wharehouse (future development) - or to go on Batch - creation date

        this.deviceImportService.addDeviceToBatch(newDevice, info.batch);
        return DeviceInfo.from(newDevice, getSlaveDevicesForDevice(newDevice), deviceImportService, topologyService, issueService);
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
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteDevice(@PathParam("mRID") String id) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(id);
        device.delete();
        return Response.ok().build();
    }

    @GET
    @Path("/{mRID}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE,Privileges.VIEW_DEVICE})
    public DeviceInfo findDeviceTypeBymRID(@PathParam("mRID") String id, @Context SecurityContext securityContext) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(id);
        return DeviceInfo.from(device, getSlaveDevicesForDevice(device), deviceImportService, topologyService, issueService);
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
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{mRID}/messagecategories")
    public PagedInfoList getAllAvailableDeviceCategoriesIncludingMessageSpecsForCurrentUser(@PathParam("mRID") String mrid, @BeanParam QueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);

        Set<DeviceMessageId> supportedMessagesSpecs = device.getDeviceType().getDeviceProtocolPluggableClass().getDeviceProtocol().getSupportedMessages();

        List<DeviceMessageId> enabledDeviceMessageIds = device.getDeviceConfiguration().getDeviceMessageEnablements().stream().map(DeviceMessageEnablement::getDeviceMessageId).collect(Collectors.toList());
        List<DeviceMessageCategoryInfo> infos = new ArrayList<>();

        deviceMessageSpecificationService.allCategories().stream().sorted((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName())).forEach(category -> {
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
        return PagedInfoList.asJson("categories", deviceMessageCategoryInfosInPage, queryParameters);
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

    @Path("/{mRID}/validationrulesets")
    public DeviceValidationResource getDeviceConfigurationResource() {
        return deviceValidationResourceProvider.get();
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

    @Path("/{mRID}/devicelabels")
    public DeviceLabelResource getDeviceLabelResource() {
        return deviceLabelResourceProvider.get();
    }
    
    @Path("/{mRID}/connections")
    public ConnectionResource getConnectionResource() {
        return connectionResourceProvider.get();
    }
    
    @Path("/{mRID}/communications")
    public CommunicationResource getCommunicationResource() {
        return communicationResourceProvider.get();
    }

    @GET
    @Path("/{mRID}/topology/communication")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_DEVICE)
    public PagedInfoList getCommunicationReferences(@PathParam("mRID") String id, @BeanParam QueryParameters queryParameters, @BeanParam JsonQueryFilter filter) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(id);
        TopologyTimeline timeline = topologyService.getPysicalTopologyTimeline(device);
        Predicate<Device> filterPredicate = getFilterForCommunicationTopology(filter);
        Stream<Device> stream = timeline.getAllDevices().stream().filter(filterPredicate)
                .sorted(Comparator.comparing(Device::getmRID));
        if (queryParameters.getStart() != null && queryParameters.getStart() > 0) {
            stream = stream.skip(queryParameters.getStart());
        }
        if (queryParameters.getLimit() != null && queryParameters.getLimit() > 0) {
            stream = stream.limit(queryParameters.getLimit() + 1);
        }
        List<DeviceTopologyInfo> topologyList = stream.map(d -> DeviceTopologyInfo.from(d, timeline.mostRecentlyAddedOn(d))).collect(Collectors.toList());
        return PagedInfoList.asJson("slaveDevices", topologyList, queryParameters);
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
