package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.rest.CompletionCodeAdapter;
import com.energyict.mdc.device.data.rest.TaskStatusAdapter;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFilterSpecification;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.TaskService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.*;

@Path("/communications")
public class CommunicationResource {

    private static final TaskStatusAdapter TASK_STATUS_ADAPTER = new TaskStatusAdapter();
    private static final CompletionCodeAdapter COMPLETION_CODE_ADAPTER = new CompletionCodeAdapter();

    private final CommunicationTaskService communicationTaskService;
    private final SchedulingService schedulingService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final TaskService taskService;
    private final ComTaskExecutionInfoFactory comTaskExecutionInfoFactory;
    private final MeteringGroupsService meteringGroupsService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public CommunicationResource(CommunicationTaskService communicationTaskService, SchedulingService schedulingService, DeviceConfigurationService deviceConfigurationService, TaskService taskService, ComTaskExecutionInfoFactory comTaskExecutionInfoFactory, MeteringGroupsService meteringGroupsService, ExceptionFactory exceptionFactory) {
        this.communicationTaskService = communicationTaskService;
        this.schedulingService = schedulingService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.taskService = taskService;
        this.comTaskExecutionInfoFactory = comTaskExecutionInfoFactory;
        this.meteringGroupsService = meteringGroupsService;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Consumes("application/json")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response getCommunications(@BeanParam JsonQueryFilter jsonQueryFilter, @BeanParam QueryParameters queryParameters) throws Exception {
        ComTaskExecutionFilterSpecification filter = buildFilterFromJsonQuery(jsonQueryFilter);
        if (queryParameters.getStart() == null || queryParameters.getLimit() == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        List<ComTaskExecution> communicationTasksByFilter = communicationTaskService.findComTaskExecutionsByFilter(filter, queryParameters.getStart(), queryParameters.getLimit() + 1);
        List<ComTaskExecutionInfo> comTaskExecutionInfos = new ArrayList<>(communicationTasksByFilter.size());
        for (ComTaskExecution comTaskExecution : communicationTasksByFilter) {
            java.util.Optional<ComTaskExecutionSession> lastComTaskExecutionSession = communicationTaskService.findLastSessionFor(comTaskExecution);
            comTaskExecutionInfos.add(comTaskExecutionInfoFactory.from(comTaskExecution, lastComTaskExecutionSession));
        }

        return Response.ok(PagedInfoList.fromPagedList("communicationTasks", comTaskExecutionInfos, queryParameters)).build();
    }

    @PUT
    @Path("/{comTaskExecId}/run")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response runCommunication(@PathParam("comTaskExecId") long comTaskExecId) {
        ComTaskExecution comTaskExecution = communicationTaskService.findComTaskExecution(comTaskExecId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_COMMUNICATION_TASK, comTaskExecId));
            comTaskExecution.scheduleNow();
                return Response.status(Response.Status.OK).build();
    }

    @PUT
    @Path("/{comTaskExecId}/runnow")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response runCommunicationNow(@PathParam("comTaskExecId") long comTaskExecId) {
        ComTaskExecution comTaskExecution = communicationTaskService.findComTaskExecution(comTaskExecId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_COMMUNICATION_TASK, comTaskExecId));
            comTaskExecution.runNow();
        return Response.status(Response.Status.OK).build();
    }

    private ComTaskExecutionFilterSpecification buildFilterFromJsonQuery(JsonQueryFilter jsonQueryFilter) throws Exception {
        ComTaskExecutionFilterSpecification filter = new ComTaskExecutionFilterSpecification();

        filter.taskStatuses = EnumSet.noneOf(TaskStatus.class);
        if (jsonQueryFilter.hasProperty(FilterOption.currentStates.name())) {
            List<TaskStatus> taskStatuses = jsonQueryFilter.getPropertyList(FilterOption.currentStates.name(), TASK_STATUS_ADAPTER);
            filter.taskStatuses.addAll(taskStatuses);
        }

        filter.latestResults = EnumSet.noneOf(CompletionCode.class);
        if (jsonQueryFilter.hasProperty(FilterOption.latestResults.name())) {
            List<CompletionCode> latestResults = jsonQueryFilter.getPropertyList(FilterOption.latestResults.name(), COMPLETION_CODE_ADAPTER);
            filter.latestResults.addAll(latestResults);
        }

        filter.comSchedules = new HashSet<>();
        if (jsonQueryFilter.hasProperty(FilterOption.comSchedules.name())) {
            List<Long> comScheduleIds = jsonQueryFilter.getLongList(FilterOption.comSchedules.name());
            filter.comSchedules.addAll(getObjectsByIdFromList(comScheduleIds, schedulingService.findAllSchedules()));
        }

        filter.comTasks = new HashSet<>();
        if (jsonQueryFilter.hasProperty(FilterOption.comTasks.name())) {
            List<Long> comTaskIds = jsonQueryFilter.getLongList(FilterOption.comTasks.name());
            filter.comTasks.addAll(getObjectsByIdFromList(comTaskIds, taskService.findAllComTasks()));
        }

        filter.deviceTypes = new HashSet<>();
        if (jsonQueryFilter.hasProperty(HeatMapBreakdownOption.deviceTypes.name())) {
            List<Long> deviceTypeIds = jsonQueryFilter.getLongList(HeatMapBreakdownOption.deviceTypes.name());
            filter.deviceTypes.addAll(getObjectsByIdFromList(deviceTypeIds, deviceConfigurationService.findAllDeviceTypes().find()));
        }

        if (jsonQueryFilter.hasProperty(FilterOption.deviceGroups.name())) {
            filter.deviceGroups = new HashSet<>();
            jsonQueryFilter.getLongList(FilterOption.deviceGroups.name()).stream().forEach(id -> filter.deviceGroups.add(meteringGroupsService.findEndDeviceGroup(id).get()));
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
