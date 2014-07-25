package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.LocalizedException;
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
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.elster.jupiter.util.conditions.Where.where;

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
    private final Provider<DeviceValidationResource> deviceValidationResourceProvider;
    private final Provider<RegisterResource> registerResourceProvider;
    private final ExceptionFactory exceptionFactory;
    private final SchedulingService schedulingService;
    private final Thesaurus thesaurus;
    private final String ALL = "all";

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
            Provider<RegisterResource> registerResourceProvider,
            ExceptionFactory exceptionFactory,
            SchedulingService schedulingService,
            Provider<DeviceValidationResource> deviceValidationResourceProvider,
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
        this.registerResourceProvider = registerResourceProvider;
        this.deviceValidationResourceProvider = deviceValidationResourceProvider;
        this.exceptionFactory = exceptionFactory;
        this.schedulingService = schedulingService;
        this.thesaurus = thesaurus;
    }
	
	@GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getAllDevices(@BeanParam QueryParameters queryParameters, @BeanParam StandardParametersBean params) {
        Condition condition = getQueryCondition(params);
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

    private Condition getQueryCondition(StandardParametersBean params) {
        Condition condition = Condition.TRUE;
        if(params.getQueryParameters().size() > 0) {
            condition = condition.and(addDeviceQueryCondition(params));
        }
        return condition;
    }

    private Condition addDeviceQueryCondition(StandardParametersBean params) {
        Condition conditionDevice = Condition.TRUE;
        String mRID = params.getFirst("mRID");
        if (mRID != null) {
            conditionDevice =  !params.isRegExp()
                    ? conditionDevice.and(where("mRID").isEqualTo(mRID))
                    : conditionDevice.and(where("mRID").likeIgnoreCase(mRID));
        }
        String serialNumber = params.getFirst("serialNumber");
        if (serialNumber != null) {
            conditionDevice =  !params.isRegExp()
                    ? conditionDevice.and(where("serialNumber").isEqualTo(serialNumber))
                    : conditionDevice.and(where("serialNumber").likeIgnoreCase(serialNumber));
        }
        String deviceType = params.getFirst("deviceTypeName");
        if (deviceType != null) {
            conditionDevice = conditionDevice.and(createMultipleConditions(deviceType,"deviceConfiguration.deviceType.name"));
        }
        String deviceConfiguration = params.getFirst("deviceConfigurationName");
        if (deviceConfiguration != null) {
            conditionDevice = conditionDevice.and(createMultipleConditions(deviceConfiguration,"deviceConfiguration.name"));
        }
        return conditionDevice;
    }

    private Condition createMultipleConditions(String params, String conditionField) {
        Condition condition = Condition.FALSE;
        String[] values = params.split(",");
        for (String value : values) {
            condition = condition.or(where(conditionField).isEqualTo(value.trim()));
        }
        return condition;
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

    @PUT
    @Path("/schedules")
    public Response addComScheduleToDeviceSet(BulkRequestInfo request, @BeanParam QueryParameters queryParameters){
        BulkAction action = new BulkAction() {
            @Override
            public void doAction(Device device, ComSchedule schedule) {
                device.newScheduledComTaskExecution(schedule).add();
            }
        };
        ComSchedulesBulkInfo response = processBulkActionsForComSchedule(request, action, queryParameters.getBoolean(ALL));
        return Response.ok(response.build()).build();
    }

    @DELETE
    @Path("/schedules")
    public Response deleteComScheduleFromDeviceSet(BulkRequestInfo request, @BeanParam QueryParameters queryParameters){
        BulkAction action = new BulkAction() {
            @Override
            public void doAction(Device device, ComSchedule schedule) {
                device.removeComSchedule(schedule);
            }
        };
        ComSchedulesBulkInfo response = processBulkActionsForComSchedule(request, action, queryParameters.getBoolean(ALL));
        return Response.ok(response.build()).build();
    }

    private ComSchedulesBulkInfo processBulkActionsForComSchedule(BulkRequestInfo request, BulkAction action, boolean allDevices) {
        ComSchedulesBulkInfo response = new ComSchedulesBulkInfo();
        Map<String, Device> deviceMap = getDeviceMapForBulkAction(request, response, allDevices);
        for (Long scheduleId : request.scheduleIds) {
            Optional<ComSchedule> scheduleRef = schedulingService.findSchedule(scheduleId);
            if (!scheduleRef.isPresent()){
                String failMessage = MessageSeeds.NO_SUCH_COM_SCHEDULE.formate(thesaurus, scheduleId);
                response.nextAction(failMessage).failCount = deviceMap.size();
            } else {
                processScheduleForBulkAction(deviceMap, scheduleRef.get(), action, response);
            }
        }
        return response;
    }

    private void processScheduleForBulkAction(Map<String, Device> deviceMap, ComSchedule schedule, BulkAction action, ComSchedulesBulkInfo response) {
        response.nextAction(schedule.getName());
        for (Device device : deviceMap.values()) {
            processSchedule(device, schedule, action, response);
        }
    }

    private void processSchedule (Device device, ComSchedule schedule, BulkAction action, ComSchedulesBulkInfo response) {
        try{
            action.doAction(device, schedule);
            device.save();
            response.success();
        } catch (LocalizedException localizedEx){
            response.fail(DeviceInfo.from(device), localizedEx.getLocalizedMessage(), localizedEx.getClass().getSimpleName());
        } catch (ConstraintViolationException validationException){
            response.fail(DeviceInfo.from(device),getMessageForConstraintViolation(validationException, device, schedule),
                    validationException.getClass().getSimpleName());
        }
    }

    private Map<String, Device> getDeviceMapForBulkAction(BulkRequestInfo request, ComSchedulesBulkInfo response, boolean allDevices) {
        Map<String, Device> deviceMap = new HashMap<>();
        if(allDevices) {
            List<Device> devices = deviceDataService.findAllDevices();
            for(Device device : devices) {
                deviceMap.put(device.getmRID(), device);
            }
        } else {
            for (String mrid : request.deviceMRIDs) {
                try {
                    deviceMap.put(mrid, resourceHelper.findDeviceByMrIdOrThrowException(mrid));
                } catch (LocalizedException ex){
                    DeviceInfo deviceInfo = new DeviceInfo();
                    deviceInfo.mRID = mrid;
                    response.generalFail(deviceInfo, ex.getLocalizedMessage(), ex.getClass().getSimpleName());
                }
            }
        }

        return deviceMap;
    }

    private String getMessageForConstraintViolation(ConstraintViolationException ex, Device device, ComSchedule schedule) {
        if (ex.getConstraintViolations() != null && ex.getConstraintViolations().size() > 0){
            return ex.getConstraintViolations().iterator().next().getMessage();
        }
        return MessageSeeds.DEVICE_VALIDATION_BULK_MSG.formate(thesaurus, schedule.getName(), device.getName());
    }

    private static interface BulkAction {
        public void doAction(Device device, ComSchedule schedule);
    }
}
