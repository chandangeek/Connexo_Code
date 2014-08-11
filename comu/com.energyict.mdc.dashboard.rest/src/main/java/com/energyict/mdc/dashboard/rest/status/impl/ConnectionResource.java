package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.JsonQueryFilter;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFilterSpecification;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.tasks.history.ComSession;
import com.energyict.mdc.tasks.history.TaskHistoryService;
import com.google.common.base.Optional;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/connections")
public class ConnectionResource {

    private static final String TASK_STATUSES = "taskStatuses";
    private static final TaskStatusAdapter TASK_STATUS_ADAPTER = new TaskStatusAdapter();

    private final Thesaurus thesaurus;
    private final DeviceDataService deviceDataService;
    private final EngineModelService engineModelService;
    private final TaskHistoryService taskHistoryService;

    @Inject
    public ConnectionResource(Thesaurus thesaurus, DeviceDataService deviceDataService, EngineModelService engineModelService, TaskHistoryService taskHistoryService) {
        this.thesaurus = thesaurus;
        this.deviceDataService = deviceDataService;
        this.engineModelService = engineModelService;
        this.taskHistoryService = taskHistoryService;
    }

    @GET
    @Consumes("application/json")
    public Response getConnections(@BeanParam JsonQueryFilter jsonQueryFilter, @BeanParam QueryParameters queryParameters) throws Exception {
        ConnectionTaskFilterSpecification filter = new ConnectionTaskFilterSpecification();
        filter.taskStatuses = new HashSet<>();
        if (jsonQueryFilter.getFilterProperties().containsKey(TASK_STATUSES)) {
            String[] taskStatuses = jsonQueryFilter.getFilterProperties().get("taskStatuses").split(",");
            for (String taskStatus : taskStatuses) {
                filter.taskStatuses.add(TASK_STATUS_ADAPTER.unmarshal(taskStatus));
            }
        }

        filter.comPortPools = new HashSet<>();
        if (jsonQueryFilter.getFilterProperties().containsKey("comPortPools")) {
            String[] comPortPoolIds = jsonQueryFilter.getFilterProperties().get("comPortPools").split(",");

            for (ComPortPool comPortPool : engineModelService.findAllComPortPools()) {
                String comPortPoolIdString = ""+comPortPool.getId();
                for (String comPortPoolId : comPortPoolIds) {
                    if (comPortPoolIdString.equals(comPortPoolId)) {
                        filter.comPortPools.add(comPortPool);
                    }
                }
            }
        }

        List<ConnectionTask> connectionTasksByFilter = deviceDataService.findConnectionTasksByFilter(filter);
        List<ConnectionTaskInfo> connectionTaskInfos = new ArrayList<>(connectionTasksByFilter.size());
        for (ConnectionTask<?,?> connectionTask : connectionTasksByFilter) {
            Optional<ComSession> lastComSession = taskHistoryService.getLastComSession(connectionTask);

            connectionTaskInfos.add(ConnectionTaskInfo.from(connectionTask, thesaurus, lastComSession));
        }

        return Response.ok(PagedInfoList.asJson("connectionTasks", connectionTaskInfos, queryParameters)).build();
    }

}
