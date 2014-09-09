package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.rest.JsonQueryFilter;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.rest.TaskStatusAdapter;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFilterSpecification;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.TaskService;
import com.google.common.base.Optional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/communications")
public class CommunicationResource {

    private static final TaskStatusAdapter TASK_STATUS_ADAPTER = new TaskStatusAdapter();
    private static final CompletionCodeAdapter COMPLETION_CODE_ADAPTER = new CompletionCodeAdapter();

    private final DeviceDataService deviceDataService;
    private final SchedulingService schedulingService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final TaskService taskService;
    private final ComTaskExecutionInfoFactory comTaskExecutionInfoFactory;

    @Inject
    public CommunicationResource(DeviceDataService deviceDataService, SchedulingService schedulingService, DeviceConfigurationService deviceConfigurationService, TaskService taskService, ComTaskExecutionInfoFactory comTaskExecutionInfoFactory) {
        this.deviceDataService = deviceDataService;
        this.schedulingService = schedulingService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.taskService = taskService;
        this.comTaskExecutionInfoFactory = comTaskExecutionInfoFactory;
    }

    @GET
    @Consumes("application/json")
    public Response getCommunications(@BeanParam JsonQueryFilter jsonQueryFilter, @BeanParam QueryParameters queryParameters) throws Exception {
        ComTaskExecutionFilterSpecification filter = buildFilterFromJsonQuery(jsonQueryFilter);
        if (queryParameters.getStart()==null || queryParameters.getLimit()==null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        List<ComTaskExecution> communicationTasksByFilter = deviceDataService.findComTaskExecutionsByFilter(filter, queryParameters.getStart(), queryParameters.getLimit() + 1);
        List<ComTaskExecutionInfo> comTaskExecutionInfos = new ArrayList<>(communicationTasksByFilter.size());
        for (ComTaskExecution comTaskExecution : communicationTasksByFilter) {
            Optional<ComTaskExecutionSession> lastSession = deviceDataService.findLastSessionFor(comTaskExecution);
            ConnectionTask<?, ?> connectionTask = comTaskExecution.getConnectionTask();
            if (connectionTask!=null) {
                comTaskExecutionInfos.add(comTaskExecutionInfoFactory.from(comTaskExecution, lastSession, connectionTask));
            } else {
                comTaskExecutionInfos.add(comTaskExecutionInfoFactory.from(comTaskExecution, lastSession));
            }
        }

        return Response.ok(PagedInfoList.asJson("communicationTasks", comTaskExecutionInfos, queryParameters)).build();
    }

    private ComTaskExecutionFilterSpecification buildFilterFromJsonQuery(JsonQueryFilter jsonQueryFilter) throws Exception {
        ComTaskExecutionFilterSpecification filter = new ComTaskExecutionFilterSpecification();
        Map<String, String> filterProperties = jsonQueryFilter.getFilterProperties();

        filter.taskStatuses = EnumSet.noneOf(TaskStatus.class);
        if (filterProperties.containsKey(FilterOption.currentStates.name())) {
            String[] taskStatuses = filterProperties.get(FilterOption.currentStates.name()).split(",");
            for (String taskStatus : taskStatuses) {
                filter.taskStatuses.add(TASK_STATUS_ADAPTER.unmarshal(taskStatus));
            }
        }

        filter.latestResults = EnumSet.noneOf(CompletionCode.class);
        if (filterProperties.containsKey(FilterOption.latestResults.name())) {
            String[] latestResults = filterProperties.get(FilterOption.latestResults.name()).split(",");
            for (String completionCode : latestResults) {
                filter.latestResults.add(COMPLETION_CODE_ADAPTER.unmarshal(completionCode));
            }
        }

        filter.comSchedules = new HashSet<>();
        if (filterProperties.containsKey(FilterOption.comSchedules.name())) {
            String[] comScheduleIds = filterProperties.get(FilterOption.comSchedules.name()).split(",");
            filter.comSchedules.addAll(getObjectsByIdFromList(comScheduleIds, schedulingService.findAllSchedules()));
        }

        filter.comTasks = new HashSet<>();
        if (filterProperties.containsKey(FilterOption.comTasks.name())) {
            String[] comTaskIds = filterProperties.get(FilterOption.comTasks.name()).split(",");
            filter.comTasks.addAll(getObjectsByIdFromList(comTaskIds, taskService.findAllComTasks()));
        }

        filter.deviceTypes = new HashSet<>();
        if (filterProperties.containsKey(HeatMapBreakdownOption.deviceTypes.name())) {
            String[] deviceTypeIds = filterProperties.get(HeatMapBreakdownOption.deviceTypes.name()).split(",");
            filter.deviceTypes.addAll(getObjectsByIdFromList(deviceTypeIds, deviceConfigurationService.findAllDeviceTypes().find()));
        }


        if (filterProperties.containsKey(FilterOption.startIntervalFrom.name()) || filterProperties.containsKey(FilterOption.startIntervalTo.name())) {
            Date start=null;
            Date end=null;
            if (filterProperties.containsKey(FilterOption.startIntervalFrom.name())) {
                start=new Date(Long.parseLong(filterProperties.get(FilterOption.startIntervalFrom.name())));
            }
            if (filterProperties.containsKey(FilterOption.startIntervalTo.name())) {
                end=new Date(Long.parseLong(filterProperties.get(FilterOption.startIntervalTo.name())));
            }
            filter.lastSessionStart=new Interval(start, end);
        }

        if (filterProperties.containsKey(FilterOption.finishIntervalFrom.name()) || filterProperties.containsKey(FilterOption.finishIntervalTo.name())) {
            Date start=null;
            Date end=null;
            if (filterProperties.containsKey(FilterOption.finishIntervalFrom.name())) {
                start=new Date(Long.parseLong(filterProperties.get(FilterOption.finishIntervalFrom.name())));
            }
            if (filterProperties.containsKey(FilterOption.finishIntervalTo.name())) {
                end=new Date(Long.parseLong(filterProperties.get(FilterOption.finishIntervalTo.name())));
            }
            filter.lastSessionEnd=new Interval(start, end);
        }

        return filter;
    }

    private <H extends HasId> Collection<H> getObjectsByIdFromList(String[] ids, List<H> objects) {
        List<H> selectedObjects = new ArrayList<>(ids.length);
        for (H object : objects) {
            String objectIdString = ""+object.getId();
            for (String id : ids) {
                if (objectIdString.equals(id.trim())) {
                    selectedObjects.add(object);
                }
            }
        }
        return selectedObjects;
    }

}
