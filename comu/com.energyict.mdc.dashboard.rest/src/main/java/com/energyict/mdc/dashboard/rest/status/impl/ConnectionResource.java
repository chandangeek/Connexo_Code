package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.common.rest.JsonQueryFilter;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.rest.TaskStatusAdapter;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFilterSpecification;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.engine.model.security.Privileges;
import com.google.common.base.Optional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/connections")
public class ConnectionResource {

    private static final Comparator<ComTaskExecution> COM_TASK_EXECUTION_COMPARATOR = new ComTaskExecutionComparator();

    private static final TaskStatusAdapter TASK_STATUS_ADAPTER = new TaskStatusAdapter();

    private static final String TASK_STATUSES = "currentStates";
    private static final String COM_PORT_POOLS = "comPortPools";
    private static final String CONNECTION_TYPES = "connectionTypes";
    private static final String DEVICE_TYPES = "deviceTypes";
    private static final String START_INTERVAL_FROM = "startIntervalFrom";
    private static final String START_INTERVAL_TO = "startIntervalTo";
    private static final String FINISH_INTERVAL_FROM = "finishIntervalFrom";
    private static final String FINISH_INTERVAL_TO = "finishIntervalTo";

    private final Thesaurus thesaurus;
    private final DeviceDataService deviceDataService;
    private final EngineModelService engineModelService;
    private final ProtocolPluggableService protocolPluggableService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final ConnectionTaskInfoFactory connectionTaskInfoFactory;

    @Inject
    public ConnectionResource(Thesaurus thesaurus, DeviceDataService deviceDataService, EngineModelService engineModelService, ProtocolPluggableService protocolPluggableService, DeviceConfigurationService deviceConfigurationService, ConnectionTaskInfoFactory connectionTaskInfoFactory) {
        this.thesaurus = thesaurus;
        this.deviceDataService = deviceDataService;
        this.engineModelService = engineModelService;
        this.protocolPluggableService = protocolPluggableService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.connectionTaskInfoFactory = connectionTaskInfoFactory;
    }

    @GET
    @Path("/connectiontypepluggableclasses")
    @Produces("application/json")
    @RolesAllowed(Privileges.VIEW_COMSERVER)
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
    @Consumes("application/json")
    @RolesAllowed(Privileges.VIEW_COMSERVER)
    public Response getConnections(@BeanParam JsonQueryFilter jsonQueryFilter, @BeanParam QueryParameters queryParameters) throws Exception {
        ConnectionTaskFilterSpecification filter = buildFilterFromJsonQuery(jsonQueryFilter);
        if (queryParameters.getStart()==null || queryParameters.getLimit()==null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        List<ConnectionTask> connectionTasksByFilter = deviceDataService.findConnectionTasksByFilter(filter, queryParameters.getStart(), queryParameters.getLimit()+1);
        List<ConnectionTaskInfo> connectionTaskInfos = new ArrayList<>(connectionTasksByFilter.size());
        for (ConnectionTask<?,?> connectionTask : connectionTasksByFilter) {
            Optional<ComSession> lastComSession = connectionTask.getLastComSession();
            List<ComTaskExecution> comTaskExecutions = deviceDataService.findComTaskExecutionsByConnectionTask(connectionTask);
            Collections.sort(comTaskExecutions, COM_TASK_EXECUTION_COMPARATOR);
            connectionTaskInfos.add(connectionTaskInfoFactory.from(connectionTask, lastComSession, comTaskExecutions));
        }

        return Response.ok(PagedInfoList.asJson("connectionTasks", connectionTaskInfos, queryParameters)).build();
    }

    private ConnectionTaskFilterSpecification buildFilterFromJsonQuery(JsonQueryFilter jsonQueryFilter) throws Exception {
        ConnectionTaskFilterSpecification filter = new ConnectionTaskFilterSpecification();
        filter.taskStatuses = EnumSet.noneOf(TaskStatus.class);
        Map<String, String> filterProperties = jsonQueryFilter.getFilterProperties();
        if (filterProperties.containsKey(TASK_STATUSES)) {
            String[] taskStatuses = filterProperties.get(TASK_STATUSES).split(",");
            for (String taskStatus : taskStatuses) {
                filter.taskStatuses.add(TASK_STATUS_ADAPTER.unmarshal(taskStatus));
            }
        }

        filter.comPortPools = new HashSet<>();
        if (filterProperties.containsKey(COM_PORT_POOLS)) {
            String[] comPortPoolIds = filterProperties.get(COM_PORT_POOLS).split(",");
            // already optimized
            for (ComPortPool comPortPool : engineModelService.findAllComPortPools()) {
                String comPortPoolIdString = ""+comPortPool.getId();
                for (String comPortPoolId : comPortPoolIds) {
                    if (comPortPoolIdString.equals(comPortPoolId)) {
                        filter.comPortPools.add(comPortPool);
                    }
                }
            }
        }

        filter.connectionTypes = new HashSet<>();
        if (filterProperties.containsKey(CONNECTION_TYPES)) {
            List<String> connectionTypeIds = Arrays.asList(filterProperties.get(CONNECTION_TYPES).split(","));
            for (String connectionTypeId : connectionTypeIds) {
                filter.connectionTypes.add(protocolPluggableService.findConnectionTypePluggableClass(Integer.parseInt(connectionTypeId)));
            }
        }

        filter.deviceTypes = new HashSet<>();
        if (filterProperties.containsKey(DEVICE_TYPES)) {
            List<String> deviceTypeIds = Arrays.asList(filterProperties.get(DEVICE_TYPES).split(","));
            for (String deviceTypeId : deviceTypeIds) {
                filter.deviceTypes.add(deviceConfigurationService.findDeviceType(Integer.parseInt(deviceTypeId)));
            }
        }

        if (filterProperties.containsKey(START_INTERVAL_FROM) || filterProperties.containsKey(START_INTERVAL_TO)) {
            Date start=null;
            Date end=null;
            if (filterProperties.containsKey(START_INTERVAL_FROM)) {
                start=new Date(Long.parseLong(filterProperties.get(START_INTERVAL_FROM)));
            }
            if (filterProperties.containsKey(START_INTERVAL_TO)) {
                end=new Date(Long.parseLong(filterProperties.get(START_INTERVAL_TO)));
            }
            filter.lastSessionStart=new Interval(start, end);
        }

        if (filterProperties.containsKey(FINISH_INTERVAL_FROM) || filterProperties.containsKey(FINISH_INTERVAL_TO)) {
            Date start=null;
            Date end=null;
            if (filterProperties.containsKey(FINISH_INTERVAL_FROM)) {
                start=new Date(Long.parseLong(filterProperties.get(FINISH_INTERVAL_FROM)));
            }
            if (filterProperties.containsKey(FINISH_INTERVAL_TO)) {
                end=new Date(Long.parseLong(filterProperties.get(FINISH_INTERVAL_TO)));
            }
            filter.lastSessionEnd=new Interval(start, end);
        }



        return filter;
    }

}
