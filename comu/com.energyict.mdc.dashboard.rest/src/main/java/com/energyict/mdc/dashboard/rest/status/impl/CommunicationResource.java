package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.rest.DateAdapter;
import com.energyict.mdc.common.rest.JsonQueryFilter;
import com.energyict.mdc.common.rest.LongAdapter;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.rest.CompletionCodeAdapter;
import com.energyict.mdc.device.data.rest.TaskStatusAdapter;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFilterSpecification;
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
    public static final LongAdapter LONG_ADAPTER = new LongAdapter();
    public static final DateAdapter DATE_ADAPTER = new DateAdapter();

    private final CommunicationTaskService communicationTaskService;
    private final SchedulingService schedulingService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final TaskService taskService;
    private final ComTaskExecutionInfoFactory comTaskExecutionInfoFactory;
    private final MeteringGroupsService meteringGroupsService;

    @Inject
    public CommunicationResource(CommunicationTaskService communicationTaskService, SchedulingService schedulingService, DeviceConfigurationService deviceConfigurationService, TaskService taskService, ComTaskExecutionInfoFactory comTaskExecutionInfoFactory, MeteringGroupsService meteringGroupsService) {
        this.communicationTaskService = communicationTaskService;
        this.schedulingService = schedulingService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.taskService = taskService;
        this.comTaskExecutionInfoFactory = comTaskExecutionInfoFactory;
        this.meteringGroupsService = meteringGroupsService;
    }

    @GET
    @Consumes("application/json")
    public Response getCommunications(@BeanParam JsonQueryFilter jsonQueryFilter, @BeanParam QueryParameters queryParameters) throws Exception {
        ComTaskExecutionFilterSpecification filter = buildFilterFromJsonQuery(jsonQueryFilter);
        if (queryParameters.getStart() == null || queryParameters.getLimit() == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        List<ComTaskExecution> communicationTasksByFilter = communicationTaskService.findComTaskExecutionsByFilter(filter, queryParameters.getStart(), queryParameters.getLimit() + 1);
        List<ComTaskExecutionInfo> comTaskExecutionInfos = new ArrayList<>(communicationTasksByFilter.size());
        for (ComTaskExecution comTaskExecution : communicationTasksByFilter) {
            Optional<ComTaskExecutionSession> lastComTaskExecutionSession = communicationTaskService.findLastSessionFor(comTaskExecution);
            comTaskExecutionInfos.add(comTaskExecutionInfoFactory.from(comTaskExecution, lastComTaskExecutionSession));
        }

        return Response.ok(PagedInfoList.asJson("communicationTasks", comTaskExecutionInfos, queryParameters)).build();
    }

    private ComTaskExecutionFilterSpecification buildFilterFromJsonQuery(JsonQueryFilter jsonQueryFilter) throws Exception {
        ComTaskExecutionFilterSpecification filter = new ComTaskExecutionFilterSpecification();
        Map<String, Object> filterProperties = jsonQueryFilter.getFilterProperties();

        filter.taskStatuses = EnumSet.noneOf(TaskStatus.class);
        if (filterProperties.containsKey(FilterOption.currentStates.name())) {
            List<TaskStatus> taskStatuses = jsonQueryFilter.getPropertyList(FilterOption.currentStates.name(), TASK_STATUS_ADAPTER);
            filter.taskStatuses.addAll(taskStatuses);
        }

        filter.latestResults = EnumSet.noneOf(CompletionCode.class);
        if (filterProperties.containsKey(FilterOption.latestResults.name())) {
            List<CompletionCode> latestResults = jsonQueryFilter.getPropertyList(FilterOption.latestResults.name(), COMPLETION_CODE_ADAPTER);
            filter.latestResults.addAll(latestResults);
        }

        filter.comSchedules = new HashSet<>();
        if (filterProperties.containsKey(FilterOption.comSchedules.name())) {
            List<Long> comScheduleIds = jsonQueryFilter.getPropertyList(FilterOption.comSchedules.name(), LONG_ADAPTER);
            filter.comSchedules.addAll(getObjectsByIdFromList(comScheduleIds, schedulingService.findAllSchedules()));
        }

        filter.comTasks = new HashSet<>();
        if (filterProperties.containsKey(FilterOption.comTasks.name())) {
            List<Long> comTaskIds = jsonQueryFilter.getPropertyList(FilterOption.comTasks.name(), LONG_ADAPTER);
            filter.comTasks.addAll(getObjectsByIdFromList(comTaskIds, taskService.findAllComTasks()));
        }

        filter.deviceTypes = new HashSet<>();
        if (filterProperties.containsKey(HeatMapBreakdownOption.deviceTypes.name())) {
            List<Long> deviceTypeIds = jsonQueryFilter.getPropertyList(HeatMapBreakdownOption.deviceTypes.name(), LONG_ADAPTER);
            filter.deviceTypes.addAll(getObjectsByIdFromList(deviceTypeIds, deviceConfigurationService.findAllDeviceTypes().find()));
        }

        if (filterProperties.containsKey(FilterOption.deviceGroups.name())) {
            filter.deviceGroups = new HashSet<>();
            jsonQueryFilter.getPropertyList(FilterOption.deviceGroups.name(), new LongAdapter()).stream().forEach(id->filter.deviceGroups.add(meteringGroupsService.findQueryEndDeviceGroup(id).get()));
        }


        if (filterProperties.containsKey(FilterOption.startIntervalFrom.name()) || filterProperties.containsKey(FilterOption.startIntervalTo.name())) {
            Date start = null;
            Date end = null;
            if (filterProperties.containsKey(FilterOption.startIntervalFrom.name())) {
                start = jsonQueryFilter.getProperty(FilterOption.startIntervalFrom.name(), DATE_ADAPTER);
            }
            if (filterProperties.containsKey(FilterOption.startIntervalTo.name())) {
                end = jsonQueryFilter.getProperty(FilterOption.startIntervalTo.name(), DATE_ADAPTER);
            }
            filter.lastSessionStart = new Interval(start, end);
        }

        if (filterProperties.containsKey(FilterOption.finishIntervalFrom.name()) || filterProperties.containsKey(FilterOption.finishIntervalTo.name())) {
            Date start = null;
            Date end = null;
            if (filterProperties.containsKey(FilterOption.finishIntervalFrom.name())) {
                start = jsonQueryFilter.getProperty(FilterOption.finishIntervalFrom.name(), DATE_ADAPTER);
            }
            if (filterProperties.containsKey(FilterOption.finishIntervalTo.name())) {
                end = jsonQueryFilter.getProperty(FilterOption.finishIntervalTo.name(), DATE_ADAPTER);
            }
            filter.lastSessionEnd = new Interval(start, end);
        }

        return filter;
    }

    private <H extends HasId> Collection<H> getObjectsByIdFromList(List<Long> ids, List<H> objects) {
        List<H> selectedObjects = new ArrayList<>(ids.size());
        for (H object : objects) {
            for (Long id : ids) {
                if (object.getId() == id) {
                    selectedObjects.add(object);
                }
            }
        }
        return selectedObjects;
    }

}
