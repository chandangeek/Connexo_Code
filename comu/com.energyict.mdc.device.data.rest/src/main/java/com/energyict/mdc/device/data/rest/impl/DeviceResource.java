package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.imp.DeviceImportService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Calendar;
import java.util.List;

@Path("/devices")
public class DeviceResource {
    private final DeviceImportService deviceImportService;
    private final DeviceDataService deviceDataService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final ResourceHelper resourceHelper;
    private final IssueService issueService;
    private final ConnectionMethodInfoFactory connectionMethodInfoFactory;
    private final EngineModelService engineModelService;
    private final MdcPropertyUtils mdcPropertyUtils;
    private final Provider<ProtocolDialectResource> protocolDialectResourceProvider;
    private final Provider<LoadProfileResource> loadProfileResourceProvider;
    private final Provider<DeviceValidationResource> deviceValidationResourceProvider;
    private final Provider<RegisterResource> registerResourceProvider;
    private final Provider<BulkScheduleResource> bulkScheduleResourceProvider;
    private final Provider<ComtaskExecutionResource> comTaskExecutionResourceProvider;
    private final ExceptionFactory exceptionFactory;
    private final Thesaurus thesaurus;

    @Inject
    public DeviceResource(
            ResourceHelper resourceHelper,
            DeviceImportService deviceImportService,
            DeviceDataService deviceDataService,
            DeviceConfigurationService deviceConfigurationService,
            IssueService issueService,
            ConnectionMethodInfoFactory connectionMethodInfoFactory,
            EngineModelService engineModelService,
            MdcPropertyUtils mdcPropertyUtils,
            Provider<ProtocolDialectResource> protocolDialectResourceProvider,
            Provider<LoadProfileResource> loadProfileResourceProvider, Provider<RegisterResource> registerResourceProvider,
            ExceptionFactory exceptionFactory,
            Provider<DeviceValidationResource> deviceValidationResourceProvider,
            Provider<BulkScheduleResource> bulkScheduleResourceProvider,
            Provider<ComtaskExecutionResource> comTaskExecutionResourceProvider,
            Thesaurus thesaurus) {

        this.resourceHelper = resourceHelper;
        this.deviceImportService = deviceImportService;
        this.deviceDataService = deviceDataService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.issueService = issueService;
        this.connectionMethodInfoFactory = connectionMethodInfoFactory;
        this.engineModelService = engineModelService;
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.protocolDialectResourceProvider = protocolDialectResourceProvider;
        this.loadProfileResourceProvider = loadProfileResourceProvider;
        this.registerResourceProvider = registerResourceProvider;
        this.deviceValidationResourceProvider = deviceValidationResourceProvider;
        this.exceptionFactory = exceptionFactory;
        this.bulkScheduleResourceProvider = bulkScheduleResourceProvider;
        this.comTaskExecutionResourceProvider = comTaskExecutionResourceProvider;
        this.thesaurus = thesaurus;
    }
	
	@GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getAllDevices(@BeanParam QueryParameters queryParameters, @BeanParam StandardParametersBean params) {
        Condition condition = resourceHelper.getQueryConditionForDevice(params);
        Finder<Device> allDevicesFinder = deviceDataService.findAllDevices(condition);
        List<Device> allDevices = allDevicesFinder.from(queryParameters).find();
        List<DeviceInfo> deviceInfos = DeviceInfo.from(allDevices);
        return PagedInfoList.asJson("devices", deviceInfos, queryParameters);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceInfo addDevice(DeviceInfo info) {
        DeviceConfiguration deviceConfiguration = null;
        if(info.deviceConfigurationId != null){
            deviceConfiguration = deviceConfigurationService.findDeviceConfiguration(info.deviceConfigurationId);
        }

        Calendar calendar = Calendar.getInstance();
        Device newDevice = deviceDataService.newDevice(deviceConfiguration, info.mRID, info.mRID);
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
    public Response deleteDevice(@PathParam("mRID") String id) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(id);
        device.delete();
        return Response.ok().build();
    }

    @GET
    @Path("/{mRID}")
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceInfo findDeviceTypeBymRID(@PathParam("mRID") String id) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(id);
        return DeviceInfo.from(device, deviceImportService, issueService);
    }

    @GET
    @Path("/{mRID}/connectionmethods")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConnectionMethods(@PathParam("mRID") String mRID, @Context UriInfo uriInfo, @BeanParam QueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        List<ConnectionTask<?, ?>> connectionTasks = ListPager.of(device.getConnectionTasks(), new ConnectionTaskComparator()).from(queryParameters).find();
        List<ConnectionMethodInfo<?>> connectionMethodInfos = connectionMethodInfoFactory.asInfoList(connectionTasks, uriInfo);
        return Response.ok(PagedInfoList.asJson("connectionMethods", connectionMethodInfos, queryParameters)).build();
    }
	
    @POST
    @Path("/{mRID}/connectionmethods")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createConnectionMethod(@PathParam("mRID") String mrid, @Context UriInfo uriInfo, ConnectionMethodInfo<?> connectionMethodInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        PartialConnectionTask partialConnectionTask = findPartialConnectionTaskOrThrowException(device, connectionMethodInfo.name);
        ConnectionTask<?, ?> task = connectionMethodInfo.createTask(deviceDataService, engineModelService, device, mdcPropertyUtils, partialConnectionTask);
        if (connectionMethodInfo.isDefault) {
            deviceDataService.setDefaultConnectionTask(task);
        }

        return Response.status(Response.Status.CREATED).entity(connectionMethodInfoFactory.asInfo(task, uriInfo)).build();
    }

    private void pauseOrResumeTask(ConnectionMethodInfo<?> connectionMethodInfo, ConnectionTask<?, ?> task) {
        switch (connectionMethodInfo.status){
            case ACTIVE:task.activate();break;
            case INACTIVE:task.deactivate();break;
        }
    }

    @GET
    @Path("/{mRID}/connectionmethods/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConnectionMethod(@PathParam("mRID") String mrid, @PathParam("id") long connectionMethodId, @Context UriInfo uriInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        ConnectionTask<?, ?> connectionTask = findConnectionTaskOrThrowException(device, connectionMethodId);
        return Response.status(Response.Status.OK).entity(connectionMethodInfoFactory.asInfo(connectionTask, uriInfo)).build();
    }

    @PUT
    @Path("/{mRID}/connectionmethods/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateConnectionMethod(@PathParam("mRID") String mrid, @PathParam("id") long connectionMethodId, @Context UriInfo uriInfo, ConnectionMethodInfo<ConnectionTask<? extends ComPortPool, ? extends PartialConnectionTask>> connectionMethodInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        ConnectionTask<? extends ComPortPool, ? extends PartialConnectionTask> task = findConnectionTaskOrThrowException(device, connectionMethodId);
        boolean wasConnectionTaskDefault = task.isDefault();
        PartialConnectionTask partialConnectionTask = findPartialConnectionTaskOrThrowException(device, connectionMethodInfo.name);

        connectionMethodInfo.writeTo(task, partialConnectionTask, deviceDataService, engineModelService, mdcPropertyUtils);
        task.save();
        pauseOrResumeTask(connectionMethodInfo, task);
        if (connectionMethodInfo.isDefault) {
            deviceDataService.setDefaultConnectionTask(task);
        } else if (wasConnectionTaskDefault) {
            deviceDataService.clearDefaultConnectionTask(device);
        }

        device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        task = findConnectionTaskOrThrowException(device, connectionMethodId);
        return Response.status(Response.Status.OK).entity(connectionMethodInfoFactory.asInfo(task, uriInfo)).build();
    }

    private PartialConnectionTask findPartialConnectionTaskOrThrowException(Device device, String name) {
        for (PartialConnectionTask partialConnectionTask : device.getDeviceConfiguration().getPartialConnectionTasks()) {
            if (partialConnectionTask.getName().equals(name)) {
                return partialConnectionTask;
            }
        }
        throw exceptionFactory.newException(MessageSeeds.NO_SUCH_PARTIAL_CONNECTION_TASK);
    }


    @DELETE
    @Path("/{mRID}/connectionmethods/{id}")
    public Response deleteConnectionMethod(@PathParam("mRID") String mrid, @PathParam("id") long connectionMethodId) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        ConnectionTask<?,?> targetConnectionTask = findConnectionTaskOrThrowException(device, connectionMethodId);
        device.removeConnectionTask(targetConnectionTask);
        return Response.ok().build();
    }

    private ConnectionTask<?, ?> findConnectionTaskOrThrowException(Device device, long connectionMethodId) {
        for (ConnectionTask<?, ?> connectionTask : device.getConnectionTasks()) {
            if (connectionTask.getId()==connectionMethodId) {
                 return connectionTask;
            }
        }
        throw exceptionFactory.newException(MessageSeeds.NO_SUCH_CONNECTION_METHOD, device.getmRID(), connectionMethodId);
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

    @Path("/schedules")
    public BulkScheduleResource getBulkScheduleResource() {
        return bulkScheduleResourceProvider.get();
    }

    @Path("/{mRID}/schedules")
    public ComtaskExecutionResource getComTaskExecutionResource() {
        return comTaskExecutionResourceProvider.get();
    }
}
