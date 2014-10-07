package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.imp.DeviceImportService;
import com.energyict.mdc.device.data.security.Privileges;
import java.util.Calendar;
import java.util.List;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
    private final Provider<SecurityPropertySetResource> securityPropertySetResourceProvider;
    private final Provider<ConnectionMethodResource> connectionMethodResourceProvider;

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
            Provider<SecurityPropertySetResource> securityPropertySetResourceProvider,
            Provider<ConnectionMethodResource> connectionMethodResourceProvider) {

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
        this.securityPropertySetResourceProvider = securityPropertySetResourceProvider;
        this.connectionMethodResourceProvider = connectionMethodResourceProvider;
    }


	
	@GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_DEVICE)
    public PagedInfoList getAllDevices(@BeanParam QueryParameters queryParameters, @BeanParam StandardParametersBean params) {
        Condition condition = resourceHelper.getQueryConditionForDevice(params);
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
        DeviceConfiguration deviceConfiguration = null;
        if(info.deviceConfigurationId != null){
            deviceConfiguration = deviceConfigurationService.findDeviceConfiguration(info.deviceConfigurationId);
        }

        Calendar calendar = Calendar.getInstance();
        Device newDevice = deviceService.newDevice(deviceConfiguration, info.mRID, info.mRID);
        newDevice.setSerialNumber(info.serialNumber);
        calendar.set(Integer.parseInt(info.yearOfCertification), 1, 1);
        newDevice.setYearOfCertification(calendar.getTime());
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
    public DeviceComTaskResource getComTaskResource(){return deviceComTaskResourceProvider.get();}

    @Path("/{mRID}/securityproperties")
    public SecurityPropertySetResource getSecurityPropertySetResource() {
        return securityPropertySetResourceProvider.get();
    }

}
