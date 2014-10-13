package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.common.rest.JsonQueryFilter;
import com.energyict.mdc.common.rest.LongAdapter;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.rest.TaskStatusAdapter;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFilterSpecification;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.security.Privileges;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.google.common.base.Optional;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/connections")
public class ConnectionResource {

    private static final TaskStatusAdapter TASK_STATUS_ADAPTER = new TaskStatusAdapter();
    private static final ComSessionSuccessIndicatorAdapter COM_SESSION_SUCCESS_INDICATOR_ADAPTER = new ComSessionSuccessIndicatorAdapter();
    private static final ConnectionTaskSuccessIndicatorAdapter CONNECTION_TASK_SUCCESS_INDICATOR_ADAPTER = new ConnectionTaskSuccessIndicatorAdapter();
    public static final LongAdapter LONG_ADAPTER = new LongAdapter();

    private final ConnectionTaskService connectionTaskService;
    private final CommunicationTaskService communicationTaskService;
    private final EngineModelService engineModelService;
    private final ProtocolPluggableService protocolPluggableService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final ConnectionTaskInfoFactory connectionTaskInfoFactory;
    private final ExceptionFactory exceptionFactory;
    private final ComTaskExecutionInfoFactory comTaskExecutionInfoFactory;
    private final MeteringGroupsService meteringGroupsService;

    @Inject
    public ConnectionResource(ConnectionTaskService connectionTaskService, CommunicationTaskService communicationTaskService, EngineModelService engineModelService, ProtocolPluggableService protocolPluggableService, DeviceConfigurationService deviceConfigurationService, ConnectionTaskInfoFactory connectionTaskInfoFactory, ExceptionFactory exceptionFactory, ComTaskExecutionInfoFactory comTaskExecutionInfoFactory, MeteringGroupsService meteringGroupsService) {
        super();
        this.connectionTaskService = connectionTaskService;
        this.communicationTaskService = communicationTaskService;
        this.engineModelService = engineModelService;
        this.protocolPluggableService = protocolPluggableService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.connectionTaskInfoFactory = connectionTaskInfoFactory;
        this.exceptionFactory = exceptionFactory;
        this.comTaskExecutionInfoFactory = comTaskExecutionInfoFactory;
        this.meteringGroupsService = meteringGroupsService;
    }

    @GET
    @Path("/connectiontypepluggableclasses")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_COMMUNICATION_INFRASTRUCTURE)
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
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_COMMUNICATION_INFRASTRUCTURE)
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
        return Response.ok(PagedInfoList.asJson("connectionTasks", connectionTaskInfos, queryParameters)).build();
    }

    private ConnectionTaskFilterSpecification buildFilterFromJsonQuery(JsonQueryFilter jsonQueryFilter) throws Exception {
        ConnectionTaskFilterSpecification filter = new ConnectionTaskFilterSpecification();
        filter.taskStatuses = EnumSet.noneOf(TaskStatus.class);
        Map<String, Object> filterProperties = jsonQueryFilter.getFilterProperties();
        if (filterProperties.containsKey(FilterOption.currentStates.name())) {
            List<TaskStatus> taskStatuses = jsonQueryFilter.getPropertyList(FilterOption.currentStates.name(), TASK_STATUS_ADAPTER);
            filter.taskStatuses.addAll(taskStatuses);
        }

        filter.comPortPools = new HashSet<>();
        if (filterProperties.containsKey(HeatMapBreakdownOption.comPortPools.name())) {
            List<Long> comPortPoolIds = jsonQueryFilter.getPropertyList(FilterOption.comPortPools.name(), LONG_ADAPTER);
            // already optimized
            for (ComPortPool comPortPool : engineModelService.findAllComPortPools()) {
                for (Long comPortPoolId : comPortPoolIds) {
                    if (comPortPool.getId() == comPortPoolId) {
                        filter.comPortPools.add(comPortPool);
                    }
                }
            }
        }

        filter.connectionTypes = new HashSet<>();
        if (filterProperties.containsKey(HeatMapBreakdownOption.connectionTypes.name())) {
            List<Long> connectionTypeIds = jsonQueryFilter.getPropertyList(FilterOption.connectionTypes.name(), LONG_ADAPTER);
            for (Long connectionTypeId : connectionTypeIds) {
                filter.connectionTypes.add(protocolPluggableService.findConnectionTypePluggableClass(connectionTypeId));
            }
        }

        filter.latestResults = new HashSet<>();
        if (filterProperties.containsKey(FilterOption.latestResults.name())) {
            List<ComSession.SuccessIndicator> latestResults = jsonQueryFilter.getPropertyList(FilterOption.latestResults.name(), COM_SESSION_SUCCESS_INDICATOR_ADAPTER);
            filter.latestResults.addAll(latestResults);
        }

        filter.latestStatuses = new HashSet<>();
        if (filterProperties.containsKey(FilterOption.latestStates.name())) {
            List<ConnectionTask.SuccessIndicator> latestStates = jsonQueryFilter.getPropertyList(FilterOption.latestStates.name(), CONNECTION_TASK_SUCCESS_INDICATOR_ADAPTER);
            filter.latestStatuses.addAll(latestStates);
        }

        filter.deviceTypes = new HashSet<>();
        if (filterProperties.containsKey(HeatMapBreakdownOption.deviceTypes.name())) {
            List<Long> deviceTypeIds = jsonQueryFilter.getPropertyList(FilterOption.deviceTypes.name(), LONG_ADAPTER);
            for (Long deviceTypeId : deviceTypeIds) {
                filter.deviceTypes.add(deviceConfigurationService.findDeviceType(deviceTypeId));
            }
        }

        if (filterProperties.containsKey(FilterOption.startIntervalFrom.name()) || filterProperties.containsKey(FilterOption.startIntervalTo.name())) {
            Date start = null;
            Date end = null;
            if (filterProperties.containsKey(FilterOption.startIntervalFrom.name())) {
                start = jsonQueryFilter.getDate(FilterOption.startIntervalFrom.name());
            }
            if (filterProperties.containsKey(FilterOption.startIntervalTo.name())) {
                end = jsonQueryFilter.getDate(FilterOption.startIntervalTo.name());
            }
            filter.lastSessionStart = new Interval(start, end);
        }

        if (filterProperties.containsKey(FilterOption.deviceGroups.name())) {
            filter.deviceGroups = new HashSet<>();
            jsonQueryFilter.getPropertyList(FilterOption.deviceGroups.name(), new LongAdapter()).stream().forEach(id->filter.deviceGroups.add(meteringGroupsService.findQueryEndDeviceGroup(id).get()));
        }

        if (filterProperties.containsKey(FilterOption.finishIntervalFrom.name()) || filterProperties.containsKey(FilterOption.finishIntervalTo.name())) {
            Date start = null;
            Date end = null;
            if (filterProperties.containsKey(FilterOption.finishIntervalFrom.name())) {
                start = jsonQueryFilter.getDate(FilterOption.finishIntervalFrom.name());
            }
            if (filterProperties.containsKey(FilterOption.finishIntervalTo.name())) {
                end = jsonQueryFilter.getDate(FilterOption.finishIntervalTo.name());
            }
            filter.lastSessionEnd = new Interval(start, end);
        }

        return filter;
    }

    @GET
    @Path("/{connectionId}/communications")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_COMMUNICATION_INFRASTRUCTURE)
    public PagedInfoList getCommunications(@PathParam("connectionId") long connectionId, @BeanParam QueryParameters queryParameters) {
        Optional<ConnectionTask> connectionTaskOptional = connectionTaskService.findConnectionTask(connectionId);
        if (!connectionTaskOptional.isPresent()) {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_CONNECTION_TASK, connectionId);
        }
        List<ComTaskExecution> comTaskExecutions = communicationTaskService.findComTaskExecutionsByConnectionTask(connectionTaskOptional.get()).from(queryParameters).find();
        return PagedInfoList.asJson("communications",comTaskExecutionInfoFactory.from(comTaskExecutions),queryParameters);
    }

}
