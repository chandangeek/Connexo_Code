package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.common.rest.LongAdapter;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.rest.ComSessionSuccessIndicatorAdapter;
import com.energyict.mdc.device.data.rest.ConnectionTaskSuccessIndicatorAdapter;
import com.energyict.mdc.device.data.rest.TaskStatusAdapter;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFilterSpecification;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.security.Privileges;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Path("/connections")
public class ConnectionResource {

    private static final TaskStatusAdapter TASK_STATUS_ADAPTER = new TaskStatusAdapter();
    private static final ComSessionSuccessIndicatorAdapter COM_SESSION_SUCCESS_INDICATOR_ADAPTER = new ComSessionSuccessIndicatorAdapter();
    private static final ConnectionTaskSuccessIndicatorAdapter CONNECTION_TASK_SUCCESS_INDICATOR_ADAPTER = new ConnectionTaskSuccessIndicatorAdapter();
    public static final LongAdapter LONG_ADAPTER = new LongAdapter();

    private final ConnectionTaskService connectionTaskService;
    private final EngineModelService engineModelService;
    private final ProtocolPluggableService protocolPluggableService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final ConnectionTaskInfoFactory connectionTaskInfoFactory;
    private final ExceptionFactory exceptionFactory;
    private final MeteringGroupsService meteringGroupsService;
    private final ComTaskExecutionSessionInfoFactory comTaskExecutionSessionInfoFactory;

    @Inject
    public ConnectionResource(ConnectionTaskService connectionTaskService, EngineModelService engineModelService, ProtocolPluggableService protocolPluggableService, DeviceConfigurationService deviceConfigurationService, ConnectionTaskInfoFactory connectionTaskInfoFactory, ExceptionFactory exceptionFactory, MeteringGroupsService meteringGroupsService, ComTaskExecutionSessionInfoFactory comTaskExecutionSessionInfoFactory) {
        super();
        this.connectionTaskService = connectionTaskService;
        this.engineModelService = engineModelService;
        this.protocolPluggableService = protocolPluggableService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.connectionTaskInfoFactory = connectionTaskInfoFactory;
        this.exceptionFactory = exceptionFactory;
        this.meteringGroupsService = meteringGroupsService;
        this.comTaskExecutionSessionInfoFactory = comTaskExecutionSessionInfoFactory;
    }

    @GET
    @Path("/connectiontypepluggableclasses")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.ADMINISTRATE_COMMUNICATION_INFRASTRUCTURE, Privileges.VIEW_COMMUNICATION_INFRASTRUCTURE})
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
    @RolesAllowed({Privileges.ADMINISTRATE_COMMUNICATION_INFRASTRUCTURE, Privileges.VIEW_COMMUNICATION_INFRASTRUCTURE})
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
        if (jsonQueryFilter.hasProperty(FilterOption.currentStates.name())) {
            List<TaskStatus> taskStatuses = jsonQueryFilter.getPropertyList(FilterOption.currentStates.name(), TASK_STATUS_ADAPTER);
            filter.taskStatuses.addAll(taskStatuses);
        }

        filter.comPortPools = new HashSet<>();
        if (jsonQueryFilter.hasProperty(HeatMapBreakdownOption.comPortPools.name())) {
            List<Long> comPortPoolIds = jsonQueryFilter.getLongList(FilterOption.comPortPools.name());
            // already optimized
            for (ComPortPool comPortPool : engineModelService.findAllComPortPools()) {
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
                    .filter(Optional::isPresent)
                    .map(Optional::get)
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
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList()));
        }

        if (jsonQueryFilter.hasProperty(FilterOption.startIntervalFrom.name()) || jsonQueryFilter.hasProperty(FilterOption.startIntervalTo.name())) {
            Date start = null;
            Date end = null;
            if (jsonQueryFilter.hasProperty(FilterOption.startIntervalFrom.name())) {
                start = Date.from(jsonQueryFilter.getInstant(FilterOption.startIntervalFrom.name()));
            }
            if (jsonQueryFilter.hasProperty(FilterOption.startIntervalTo.name())) {
                end = Date.from(jsonQueryFilter.getInstant(FilterOption.startIntervalTo.name()));
            }
            filter.lastSessionStart = new Interval(start, end);
        }

        if (jsonQueryFilter.hasProperty(FilterOption.deviceGroups.name())) {
            filter.deviceGroups = new HashSet<>();
            jsonQueryFilter.getLongList(FilterOption.deviceGroups.name()).stream().forEach(id -> filter.deviceGroups.add(meteringGroupsService.findQueryEndDeviceGroup(id).get()));
        }

        if (jsonQueryFilter.hasProperty(FilterOption.finishIntervalFrom.name()) || jsonQueryFilter.hasProperty(FilterOption.finishIntervalTo.name())) {
            Date start = null;
            Date end = null;
            if (jsonQueryFilter.hasProperty(FilterOption.finishIntervalFrom.name())) {
                start = Date.from(jsonQueryFilter.getInstant(FilterOption.finishIntervalFrom.name()));
            }
            if (jsonQueryFilter.hasProperty(FilterOption.finishIntervalTo.name())) {
                end = Date.from(jsonQueryFilter.getInstant(FilterOption.finishIntervalTo.name()));
            }
            filter.lastSessionEnd = new Interval(start, end);
        }

        return filter;
    }

    @GET
    @Path("/{connectionId}/latestcommunications")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.ADMINISTRATE_COMMUNICATION_INFRASTRUCTURE, Privileges.VIEW_COMMUNICATION_INFRASTRUCTURE})
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

        return PagedInfoList.asJson("communications", comTaskExecutionSessionInfoFactory.from(comTaskExecutionSessions), queryParameters);
    }

}
