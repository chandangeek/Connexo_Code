package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceMessageEnablement;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.imp.DeviceImportService;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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
import javax.ws.rs.core.UriInfo;

@Path("/devices")
public class DeviceResource {
    private final DeviceImportService deviceImportService;
    private final DeviceService deviceService;
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
    private final Provider<ConnectionMethodResource> connectionMethodResourceProvider;
    private final Provider<DeviceCommandResource> deviceCommandResourceProvider;
    private final DeviceMessageService deviceMessageService;
    private final DeviceMessageSpecInfoFactory deviceMessageSpecInfoFactory;
    private final DeviceMessageCategoryInfoFactory deviceMessageCategoryInfoFactory;

    @Inject
    public DeviceResource(
            ResourceHelper resourceHelper,
            DeviceImportService deviceImportService,
            DeviceService deviceService,
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
            Provider<ConnectionMethodResource> connectionMethodResourceProvider,
            Provider<DeviceCommandResource> deviceCommandResourceProvider,
            DeviceMessageService deviceMessageService,
            DeviceMessageSpecInfoFactory deviceMessageSpecInfoFactory, DeviceMessageCategoryInfoFactory deviceMessageCategoryInfoFactory) {

        this.resourceHelper = resourceHelper;
        this.deviceImportService = deviceImportService;
        this.deviceService = deviceService;
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
        this.connectionMethodResourceProvider = connectionMethodResourceProvider;
        this.deviceCommandResourceProvider = deviceCommandResourceProvider;
        this.deviceMessageService = deviceMessageService;
        this.deviceMessageSpecInfoFactory = deviceMessageSpecInfoFactory;
        this.deviceMessageCategoryInfoFactory = deviceMessageCategoryInfoFactory;
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_DEVICE)
    public PagedInfoList getAllDevices(@BeanParam QueryParameters queryParameters, @BeanParam StandardParametersBean params,  @Context UriInfo uriInfo) {
        Condition condition = resourceHelper.getQueryConditionForDevice(uriInfo.getQueryParameters());
        MultivaluedMap<String, String> uriParams = uriInfo.getQueryParameters();
        Finder<Device> allDevicesFinder = deviceService.findAllDevices(condition);
        List<Device> allDevices = allDevicesFinder.from(queryParameters).find();
        List<DeviceInfo> deviceInfos = DeviceInfo.from(allDevices);
        return PagedInfoList.asJson("devices", deviceInfos, queryParameters);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE)
    public DeviceInfo addDevice(DeviceInfo info) {
        Optional<DeviceConfiguration> deviceConfiguration = Optional.empty();
        if (info.deviceConfigurationId != null) {
            deviceConfiguration = deviceConfigurationService.findDeviceConfiguration(info.deviceConfigurationId);
        }

        Device newDevice = deviceService.newDevice(deviceConfiguration.get(), info.mRID, info.mRID);
        newDevice.setSerialNumber(info.serialNumber);
        newDevice.setYearOfCertification(ZonedDateTime.of(Integer.parseInt(info.yearOfCertification), 1, 1, 0, 0, 0, 0, ZoneId.of("UTC")).toInstant());
        newDevice.save();

        //TODO: Device Date should go on the device wharehouse (future development) - or to go on Batch - creation date

        this.deviceImportService.addDeviceToBatch(newDevice, info.batch);
        return DeviceInfo.from(newDevice, deviceImportService, issueService);
    }

    @DELETE
    @Path("/{mRID}")
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE)
    public Response deleteDevice(@PathParam("mRID") String id) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(id);
        device.delete();
        return Response.ok().build();
    }

    @GET
    @Path("/{mRID}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_DEVICE)
    public DeviceInfo findDeviceTypeBymRID(@PathParam("mRID") String id) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(id);
        return DeviceInfo.from(device, deviceImportService, issueService);
    }

    /**
     * List all device message categories :
     * - that are supported by the device protocol defined on the device's device type
     * - that have device messages specs on them
     * - that the user has the required privileges for
     * @param mrid Device's mRID
     * @return List of categories + device message specs, indicating if message spec will be picked up by a comtask or not
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{mRID}/messagecategories")
    public List<DeviceMessageCategoryInfo> getAllAvailableDeviceCategoriesIncludingMessageSpecsForCurrentUser(@PathParam("mRID") String mrid) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);

        Set<DeviceMessageId> supportedMessagesSpecs = device.getDeviceType().getDeviceProtocolPluggableClass().getDeviceProtocol().getSupportedMessages();

        List<DeviceMessageId> enabledDeviceMessageIds = device.getDeviceConfiguration().getDeviceMessageEnablements().stream().map(DeviceMessageEnablement::getDeviceMessageId).collect(Collectors.toList());
        List<DeviceMessageCategoryInfo> infos = new ArrayList<>();

        deviceMessageService.allCategories().stream().sorted((c1,c2)->c1.getName().compareToIgnoreCase(c2.getName())).forEach(category-> {
            List<DeviceMessageSpecInfo> deviceMessageSpecs = category.getMessageSpecifications().stream()
                    .filter(deviceMessageSpec -> supportedMessagesSpecs.contains(deviceMessageSpec.getId())) // limit to device message specs supported by the protocol support
                    .peek(x -> System.err.println("Protocol supported:"+x.getName()))
                    .filter(dms -> enabledDeviceMessageIds.contains(dms.getId())) // limit to device message specs enabled on the config
                    .peek(x -> System.err.println("Config enabled supported:"+x.getName()))
                    .filter(dms->device.getDeviceConfiguration().isAuthorized(dms.getId())) // limit to device message specs whom the user is authorized to
                    .peek(x -> System.err.println("User rights:"+x.getName()))
                    .sorted((dms1, dms2) -> dms1.getName().compareToIgnoreCase(dms2.getName()))
                    .map(dms->deviceMessageSpecInfoFactory.asInfo(dms, device))
                    .collect(Collectors.toList());
            if (!deviceMessageSpecs.isEmpty()) {
                DeviceMessageCategoryInfo info = deviceMessageCategoryInfoFactory.asInfo(category);
                info.deviceMessageSpecs = deviceMessageSpecs;
                infos.add(info);
            }
        });
        return infos;
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

    @Path("/{mRID}/commands")
    public DeviceCommandResource getCommandResource() {
        return deviceCommandResourceProvider.get();
    }
}
