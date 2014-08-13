package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.JsonQueryFilter;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFilterSpecification;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.google.common.base.Optional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/connections")
public class ConnectionResource {

    private static final String TASK_STATUSES = "currentStates";
    private static final String COM_PORT_POOLS = "comPortPools";
    private static final String CONNECTION_TYPES = "connectionTypes";
    private static final String DEVICE_TYPES = "deviceTypes";
    private static final TaskStatusAdapter TASK_STATUS_ADAPTER = new TaskStatusAdapter();

    private final Thesaurus thesaurus;
    private final DeviceDataService deviceDataService;
    private final EngineModelService engineModelService;
    private final ProtocolPluggableService protocolPluggableService;
    private final DeviceConfigurationService deviceConfigurationService;

    @Inject
    public ConnectionResource(Thesaurus thesaurus, DeviceDataService deviceDataService, EngineModelService engineModelService, ProtocolPluggableService protocolPluggableService, DeviceConfigurationService deviceConfigurationService) {
        this.thesaurus = thesaurus;
        this.deviceDataService = deviceDataService;
        this.engineModelService = engineModelService;
        this.protocolPluggableService = protocolPluggableService;
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @GET
    @Path("/connectiontypepluggableclasses")
    @Produces("application/json")
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
    public Response getConnections(@BeanParam JsonQueryFilter jsonQueryFilter, @BeanParam QueryParameters queryParameters) throws Exception {
        ConnectionTaskFilterSpecification filter = buildFilterFromJsonQuery(jsonQueryFilter);
        if (queryParameters.getStart()==null || queryParameters.getLimit()==null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        List<ConnectionTask> connectionTasksByFilter = deviceDataService.findConnectionTasksByFilter(filter, queryParameters.getStart(), queryParameters.getLimit());
        List<ConnectionTaskInfo> connectionTaskInfos = new ArrayList<>(connectionTasksByFilter.size());
        for (ConnectionTask<?,?> connectionTask : connectionTasksByFilter) {
            Optional<ComSession> lastComSession = connectionTask.getLastComSession();

            connectionTaskInfos.add(ConnectionTaskInfo.from(connectionTask, thesaurus, lastComSession));
        }

        return Response.ok(PagedInfoList.asJson("connectionTasks", connectionTaskInfos, queryParameters)).build();
    }

    private ConnectionTaskFilterSpecification buildFilterFromJsonQuery(JsonQueryFilter jsonQueryFilter) throws Exception {
        ConnectionTaskFilterSpecification filter = new ConnectionTaskFilterSpecification();
        filter.taskStatuses = EnumSet.noneOf(TaskStatus.class);
        if (jsonQueryFilter.getFilterProperties().containsKey(TASK_STATUSES)) {
            String[] taskStatuses = jsonQueryFilter.getFilterProperties().get(TASK_STATUSES).split(",");
            for (String taskStatus : taskStatuses) {
                filter.taskStatuses.add(TASK_STATUS_ADAPTER.unmarshal(taskStatus));
            }
        }

        filter.comPortPools = new HashSet<>();
        if (jsonQueryFilter.getFilterProperties().containsKey(COM_PORT_POOLS)) {
            String[] comPortPoolIds = jsonQueryFilter.getFilterProperties().get(COM_PORT_POOLS).split(",");
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
        if (jsonQueryFilter.getFilterProperties().containsKey(CONNECTION_TYPES)) {
            List<String> connectionTypeIds = Arrays.asList(jsonQueryFilter.getFilterProperties().get(CONNECTION_TYPES).split(","));
            for (String connectionTypeId : connectionTypeIds) {
                filter.connectionTypes.add(protocolPluggableService.findConnectionTypePluggableClass(Integer.parseInt(connectionTypeId)));
            }
        }

        filter.deviceTypes = new HashSet<>();
        if (jsonQueryFilter.getFilterProperties().containsKey(DEVICE_TYPES)) {
            List<String> deviceTypeIds = Arrays.asList(jsonQueryFilter.getFilterProperties().get(DEVICE_TYPES).split(","));
            for (String deviceTypeId : deviceTypeIds) {
                filter.deviceTypes.add(deviceConfigurationService.findDeviceType(Integer.parseInt(deviceTypeId)));
            }
        }



        return filter;
    }

}
