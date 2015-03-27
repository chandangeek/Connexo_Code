package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.imp.QueueMessage;
import com.energyict.mdc.device.data.rest.ComSessionSuccessIndicatorAdapter;
import com.energyict.mdc.device.data.rest.ConnectionTaskSuccessIndicatorAdapter;
import com.energyict.mdc.device.data.rest.TaskStatusAdapter;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFilterSpecification;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static com.elster.jupiter.util.streams.Functions.asStream;

@Path("/connections")
public class ConnectionResource {

    private static final TaskStatusAdapter TASK_STATUS_ADAPTER = new TaskStatusAdapter();
    private static final ComSessionSuccessIndicatorAdapter COM_SESSION_SUCCESS_INDICATOR_ADAPTER = new ComSessionSuccessIndicatorAdapter();
    private static final ConnectionTaskSuccessIndicatorAdapter CONNECTION_TASK_SUCCESS_INDICATOR_ADAPTER = new ConnectionTaskSuccessIndicatorAdapter();

    private final ConnectionTaskService connectionTaskService;
    private final EngineConfigurationService engineConfigurationService;
    private final ProtocolPluggableService protocolPluggableService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final ConnectionTaskInfoFactory connectionTaskInfoFactory;
    private final ExceptionFactory exceptionFactory;
    private final MeteringGroupsService meteringGroupsService;
    private final ComTaskExecutionSessionInfoFactory comTaskExecutionSessionInfoFactory;
    private final MessageService messageService;
    private final JsonService jsonService;

    @Inject
    public ConnectionResource(ConnectionTaskService connectionTaskService, EngineConfigurationService engineConfigurationService, ProtocolPluggableService protocolPluggableService, DeviceConfigurationService deviceConfigurationService, ConnectionTaskInfoFactory connectionTaskInfoFactory, ExceptionFactory exceptionFactory, MeteringGroupsService meteringGroupsService, ComTaskExecutionSessionInfoFactory comTaskExecutionSessionInfoFactory, MessageService messageService, JsonService jsonService) {
        super();
        this.connectionTaskService = connectionTaskService;
        this.engineConfigurationService = engineConfigurationService;
        this.protocolPluggableService = protocolPluggableService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.connectionTaskInfoFactory = connectionTaskInfoFactory;
        this.exceptionFactory = exceptionFactory;
        this.meteringGroupsService = meteringGroupsService;
        this.comTaskExecutionSessionInfoFactory = comTaskExecutionSessionInfoFactory;
        this.messageService = messageService;
        this.jsonService = jsonService;
    }

    @GET
    @Path("/connectiontypepluggableclasses")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Object getConnectionTypeValues() {
        List<IdWithNameInfo> names = new ArrayList<>();
        for (ConnectionTypePluggableClass connectionTypePluggableClass : this.protocolPluggableService.findAllConnectionTypePluggableClasses()) {
            names.add(new IdWithNameInfo(connectionTypePluggableClass.getId(), connectionTypePluggableClass.getName()));
        }
        Map<String, List<IdWithNameInfo>> map = new HashMap<>();
        map.put("connectiontypepluggableclasses", names);
        return Response.ok(map).build();
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response getConnections(@BeanParam JsonQueryFilter jsonQueryFilter, @BeanParam QueryParameters queryParameters) throws Exception {
        ConnectionTaskFilterSpecification filter = buildFilterFromJsonQuery(jsonQueryFilter);
        if (queryParameters.getStart() == null || queryParameters.getLimit() == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        List<ConnectionTask> connectionTasksByFilter = connectionTaskService.findConnectionTasksByFilter(filter, queryParameters.getStart(), queryParameters.getLimit() + 1);
        List<ConnectionTaskInfo> connectionTaskInfos = new ArrayList<>(connectionTasksByFilter.size());
        for (ConnectionTask<?, ?> connectionTask : connectionTasksByFilter) {
            Optional<ComSession> lastComSession = connectionTask.getLastComSession();
            connectionTaskInfos.add(connectionTaskInfoFactory.from(connectionTask, lastComSession));
        }
        return Response.ok(PagedInfoList.fromPagedList("connectionTasks", connectionTaskInfos, queryParameters)).build();
    }

    private ConnectionTaskFilterSpecification buildFilterFromJsonQuery(JsonQueryFilter jsonQueryFilter) throws Exception {
        ConnectionTaskFilterSpecification filter = new ConnectionTaskFilterSpecification();
        filter.taskStatuses = EnumSet.noneOf(TaskStatus.class);
        if (jsonQueryFilter.hasProperty(FilterOption.currentStates.name())) {
            List<TaskStatus> taskStatuses = jsonQueryFilter.getPropertyList(FilterOption.currentStates.name(), TASK_STATUS_ADAPTER);
            filter.taskStatuses.addAll(taskStatuses);
        }

        filter.comPortPools = new HashSet<>();
        if (jsonQueryFilter.hasProperty(HeatMapBreakdownOption.comPortPools.name())) {
            List<Long> comPortPoolIds = jsonQueryFilter.getLongList(FilterOption.comPortPools.name());
            // already optimized
            for (ComPortPool comPortPool : engineConfigurationService.findAllComPortPools()) {
                for (Long comPortPoolId : comPortPoolIds) {
                    if (comPortPool.getId() == comPortPoolId) {
                        filter.comPortPools.add(comPortPool);
                    }
                }
            }
        }

        if (jsonQueryFilter.hasProperty(HeatMapBreakdownOption.connectionTypes.name())) {
            List<Long> connectionTypeIds = jsonQueryFilter.getLongList(FilterOption.connectionTypes.name());
            filter.connectionTypes = connectionTypeIds
                    .stream()
                    .map(protocolPluggableService::findConnectionTypePluggableClass)
                    .flatMap(asStream())
                    .collect(Collectors.toSet());
        }

        filter.latestResults = new HashSet<>();
        if (jsonQueryFilter.hasProperty(FilterOption.latestResults.name())) {
            List<ComSession.SuccessIndicator> latestResults = jsonQueryFilter.getPropertyList(FilterOption.latestResults.name(), COM_SESSION_SUCCESS_INDICATOR_ADAPTER);
            filter.latestResults.addAll(latestResults);
        }

        filter.latestStatuses = new HashSet<>();
        if (jsonQueryFilter.hasProperty(FilterOption.latestStates.name())) {
            List<ConnectionTask.SuccessIndicator> latestStates = jsonQueryFilter.getPropertyList(FilterOption.latestStates.name(), CONNECTION_TASK_SUCCESS_INDICATOR_ADAPTER);
            filter.latestStatuses.addAll(latestStates);
        }

        filter.deviceTypes = new HashSet<>();
        if (jsonQueryFilter.hasProperty(HeatMapBreakdownOption.deviceTypes.name())) {
            List<Long> deviceTypeIds = jsonQueryFilter.getLongList(FilterOption.deviceTypes.name());
            filter.deviceTypes.addAll(
                    deviceTypeIds.stream()
                            .map(deviceConfigurationService::findDeviceType)
                            .flatMap(asStream())
                            .collect(Collectors.toList()));
        }

        if (jsonQueryFilter.hasProperty(FilterOption.startIntervalFrom.name()) || jsonQueryFilter.hasProperty(FilterOption.startIntervalTo.name())) {
            Instant start = null;
            Instant end = null;
            if (jsonQueryFilter.hasProperty(FilterOption.startIntervalFrom.name())) {
                start = jsonQueryFilter.getInstant(FilterOption.startIntervalFrom.name());
            }
            if (jsonQueryFilter.hasProperty(FilterOption.startIntervalTo.name())) {
                end = jsonQueryFilter.getInstant(FilterOption.startIntervalTo.name());
            }
            filter.lastSessionStart = Interval.of(start, end);
        }

        if (jsonQueryFilter.hasProperty(FilterOption.deviceGroups.name())) {
            filter.deviceGroups = new HashSet<>();
            jsonQueryFilter.getLongList(FilterOption.deviceGroups.name()).stream().forEach(id -> filter.deviceGroups.add(meteringGroupsService.findEndDeviceGroup(id).get()));
        }

        if (jsonQueryFilter.hasProperty(FilterOption.finishIntervalFrom.name()) || jsonQueryFilter.hasProperty(FilterOption.finishIntervalTo.name())) {
            Instant start = null;
            Instant end = null;
            if (jsonQueryFilter.hasProperty(FilterOption.finishIntervalFrom.name())) {
                start = jsonQueryFilter.getInstant(FilterOption.finishIntervalFrom.name());
            }
            if (jsonQueryFilter.hasProperty(FilterOption.finishIntervalTo.name())) {
                end = jsonQueryFilter.getInstant(FilterOption.finishIntervalTo.name());
            }
            filter.lastSessionEnd = Interval.of(start, end);
        }

        return filter;
    }

    private ConnectionTaskFilterSpecificationMessage buildFilterMessageFromJsonQuery(JsonQueryFilter jsonQueryFilter) throws Exception {
        ConnectionTaskFilterSpecificationMessage filter = new ConnectionTaskFilterSpecificationMessage();
        filter.currentStates = new HashSet<>();
        if (jsonQueryFilter.hasProperty(FilterOption.currentStates.name())) {
            jsonQueryFilter.getPropertyList(FilterOption.currentStates.name(), TASK_STATUS_ADAPTER).stream().forEach(ts -> filter.currentStates.add(ts.name()));
        }

        filter.comPortPools = new HashSet<>();
        if (jsonQueryFilter.hasProperty(HeatMapBreakdownOption.comPortPools.name())) {
            filter.comPortPools.addAll(jsonQueryFilter.getLongList(FilterOption.comPortPools.name()));
        }

        filter.connectionTypes = new HashSet<>();
        if (jsonQueryFilter.hasProperty(HeatMapBreakdownOption.connectionTypes.name())) {
            List<Long> connectionTypeIds = jsonQueryFilter.getLongList(FilterOption.connectionTypes.name());
            connectionTypeIds
                    .stream()
                    .map(protocolPluggableService::findConnectionTypePluggableClass)
                    .flatMap(asStream())
                    .map(PluggableClass::getId)
                    .forEach(filter.connectionTypes::add);
        }

        filter.latestResults = new HashSet<>();
        if (jsonQueryFilter.hasProperty(FilterOption.latestResults.name())) {
            jsonQueryFilter.getPropertyList(FilterOption.latestResults.name(), COM_SESSION_SUCCESS_INDICATOR_ADAPTER).stream().forEach(lr->filter.latestResults.add(lr.name()));
        }

        filter.latestStates = new HashSet<>();
        if (jsonQueryFilter.hasProperty(FilterOption.latestStates.name())) {
            jsonQueryFilter.getPropertyList(FilterOption.latestStates.name(), CONNECTION_TASK_SUCCESS_INDICATOR_ADAPTER).stream().forEach(ls -> filter.latestStates.add(ls.name()));
        }

        filter.deviceTypes = new HashSet<>();
        if (jsonQueryFilter.hasProperty(HeatMapBreakdownOption.deviceTypes.name())) {
            filter.deviceTypes.addAll(jsonQueryFilter.getLongList(FilterOption.deviceTypes.name()));
        }

        if (jsonQueryFilter.hasProperty(FilterOption.startIntervalFrom.name()) || jsonQueryFilter.hasProperty(FilterOption.startIntervalTo.name())) {
            Instant start = null;
            Instant end = null;
            if (jsonQueryFilter.hasProperty(FilterOption.startIntervalFrom.name())) {
                filter.startIntervalFrom = jsonQueryFilter.getInstant(FilterOption.startIntervalFrom.name());
            }
            if (jsonQueryFilter.hasProperty(FilterOption.startIntervalTo.name())) {
                filter.startIntervalTo = jsonQueryFilter.getInstant(FilterOption.startIntervalTo.name());
            }
        }

        filter.deviceGroups = new HashSet<>();
        if (jsonQueryFilter.hasProperty(FilterOption.deviceGroups.name())) {
            filter.deviceGroups.addAll(jsonQueryFilter.getLongList(FilterOption.deviceGroups.name()));
        }

        if (jsonQueryFilter.hasProperty(FilterOption.finishIntervalFrom.name()) || jsonQueryFilter.hasProperty(FilterOption.finishIntervalTo.name())) {
            Instant start = null;
            Instant end = null;
            if (jsonQueryFilter.hasProperty(FilterOption.finishIntervalFrom.name())) {
                filter.finishIntervalFrom = jsonQueryFilter.getInstant(FilterOption.finishIntervalFrom.name());
            }
            if (jsonQueryFilter.hasProperty(FilterOption.finishIntervalTo.name())) {
                filter.finishIntervalTo = jsonQueryFilter.getInstant(FilterOption.finishIntervalTo.name());
            }
        }

        return filter;
    }

    @GET
    @Path("/{connectionId}/latestcommunications")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION})
    public PagedInfoList getCommunications(@PathParam("connectionId") long connectionId, @BeanParam QueryParameters queryParameters) {
        ConnectionTask connectionTask =
                connectionTaskService
                        .findConnectionTask(connectionId)
                        .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_CONNECTION_TASK, connectionId));
        Optional<ComSession> lastComSessionOptional = connectionTask.getLastComSession();
        List<ComTaskExecutionSession> comTaskExecutionSessions = new ArrayList<>();
        if (lastComSessionOptional.isPresent()) {
            comTaskExecutionSessions.addAll(lastComSessionOptional.get().getComTaskExecutionSessions());
        }

        return PagedInfoList.fromPagedList("communications", comTaskExecutionSessionInfoFactory.from(comTaskExecutionSessions), queryParameters);
    }

    @PUT
    @Path("/{connectionId}/run")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION})
    // TODO Would be better if this method moved to ConnectionResource in device.data.rest
    public Response runConnectionTask(@PathParam("connectionId") long connectionId, @Context UriInfo uriInfo) {
        ConnectionTask connectionTask =
                connectionTaskService
                        .findConnectionTask(connectionId)
                        .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_CONNECTION_TASK, connectionId));

        if (connectionTask instanceof ScheduledConnectionTask) {
            ((ScheduledConnectionTask) connectionTask).scheduleNow();
        } else {
            throw exceptionFactory.newException(MessageSeeds.RUN_CONNECTIONTASK_IMPOSSIBLE);
        }
        return Response.status(Response.Status.OK).build();
    }

    @PUT
    @Path("/run")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response runConnectionTask(BulkRequestInfo bulkRequestInfo) throws Exception {
        if (bulkRequestInfo!=null && bulkRequestInfo.filter!=null) {
            Optional<DestinationSpec> destinationSpec = messageService.getDestinationSpec(ConnectionTaskService.FILTER_ITEMIZER_QUEUE_DESTINATION);
            return processMessagePost(new ItemizeFilterQueueMessage(bulkRequestInfo.filter, "scheduleNow"), destinationSpec);
        } else if (bulkRequestInfo!=null && bulkRequestInfo.connections!=null) {
            Optional<DestinationSpec> destinationSpec = messageService.getDestinationSpec(ConnectionTaskService.CONNECTION_RESCHEDULER_QUEUE_DESTINATION);
            bulkRequestInfo.connections.stream().forEach(c->processMessagePost(new ConnectionTaskQueueMessage(c, "scheduleNow"), destinationSpec));
            return Response.status(Response.Status.OK).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }


    private Response processMessagePost(QueueMessage message, Optional<DestinationSpec> destinationSpec) {
        if (destinationSpec.isPresent()) {
            String json = jsonService.serialize(message);
            destinationSpec.get().message(json).send();
            return Response.ok().entity("{\"success\":\"true\"}").build();
        }
        return Response.serverError().build();
    }


    class ItemizeFilterQueueMessage implements QueueMessage {
        public ConnectionTaskFilterSpecificationMessage connectionTaskFilterSpecification;
        public String action;

        public ItemizeFilterQueueMessage(ConnectionTaskFilterSpecificationMessage connectionTaskFilterSpecification, String action) {
            this.connectionTaskFilterSpecification = connectionTaskFilterSpecification;
            this.action = action;
        }
    }

    class ConnectionTaskQueueMessage implements QueueMessage {
        public Long connectionTaskId;
        public String action;

        public ConnectionTaskQueueMessage(Long connectionTaskId, String action) {
            this.connectionTaskId = connectionTaskId;
            this.action = action;
        }
    }

}
