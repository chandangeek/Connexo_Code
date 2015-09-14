package com.energyict.mdc.dashboard.rest.status.impl;

import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.QueueMessage;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFilterSpecification;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFilterSpecificationMessage;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionQueueMessage;
import com.energyict.mdc.device.data.tasks.ItemizeCommunicationsFilterQueueMessage;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.TaskService;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.Interval;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

@Path("/communications")
public class CommunicationResource {

    private final CommunicationTaskService communicationTaskService;
    private final SchedulingService schedulingService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final TaskService taskService;
    private final ComTaskExecutionInfoFactory comTaskExecutionInfoFactory;
    private final MeteringGroupsService meteringGroupsService;
    private final ExceptionFactory exceptionFactory;
    private final JsonService jsonService;
    private final AppService appService;
    private final MessageService messageService;

    @Inject
    public CommunicationResource(CommunicationTaskService communicationTaskService, SchedulingService schedulingService, DeviceConfigurationService deviceConfigurationService, TaskService taskService, ComTaskExecutionInfoFactory comTaskExecutionInfoFactory, MeteringGroupsService meteringGroupsService, ExceptionFactory exceptionFactory, JsonService jsonService, AppService appService, MessageService messageService) {
        this.communicationTaskService = communicationTaskService;
        this.schedulingService = schedulingService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.taskService = taskService;
        this.comTaskExecutionInfoFactory = comTaskExecutionInfoFactory;
        this.meteringGroupsService = meteringGroupsService;
        this.exceptionFactory = exceptionFactory;
        this.jsonService = jsonService;
        this.appService = appService;
        this.messageService = messageService;
    }

    @GET
    @Consumes("application/json")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response getCommunications(@BeanParam JsonQueryFilter jsonQueryFilter, @BeanParam JsonQueryParameters queryParameters) throws Exception {
        ComTaskExecutionFilterSpecification filter = buildFilterFromJsonQuery(jsonQueryFilter);
        if (!queryParameters.getStart().isPresent() || !queryParameters.getLimit().isPresent()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        List<ComTaskExecution> communicationTasks = communicationTaskService.findComTaskExecutionsByFilter(filter, queryParameters.getStart().get(), queryParameters.getLimit().get() + 1);
        List<ComTaskExecutionInfo> comTaskExecutionInfos =
                communicationTasks
                        .stream()
                        .map(this::toComTaskExecutionInfo)
                        .collect(Collectors.toList());

        return Response.ok(PagedInfoList.fromPagedList("communicationTasks", comTaskExecutionInfos, queryParameters)).build();
    }

    private ComTaskExecutionInfo toComTaskExecutionInfo(ComTaskExecution comTaskExecution) {
        Optional<ComTaskExecutionSession> lastComTaskExecutionSession = this.communicationTaskService.findLastSessionFor(comTaskExecution);
        return this.comTaskExecutionInfoFactory.from(comTaskExecution, lastComTaskExecutionSession);
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

    @PUT
    @Path("/run")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response runCommunicationTask(CommunicationsBulkRequestInfo communicationsBulkRequestInfo) throws Exception {
        if (!verifyAppServerExists(CommunicationTaskService.FILTER_ITEMIZER_QUEUE_DESTINATION) || !verifyAppServerExists(CommunicationTaskService.COMMUNICATION_RESCHEDULER_QUEUE_DESTINATION)) {
            throw exceptionFactory.newException(MessageSeeds.NO_APPSERVER);
        }
        return queueCommunicationBulkAction(communicationsBulkRequestInfo, "scheduleNow");
    }

    @PUT
    @Path("/runnow")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response runCommunicationTaskNow(CommunicationsBulkRequestInfo communicationsBulkRequestInfo) throws Exception {
        if (!verifyAppServerExists(CommunicationTaskService.FILTER_ITEMIZER_QUEUE_DESTINATION) || !verifyAppServerExists(CommunicationTaskService.COMMUNICATION_RESCHEDULER_QUEUE_DESTINATION)) {
            throw exceptionFactory.newException(MessageSeeds.NO_APPSERVER);
        }
        return queueCommunicationBulkAction(communicationsBulkRequestInfo, "runNow");
    }

    private Response queueCommunicationBulkAction(CommunicationsBulkRequestInfo communicationsBulkRequestInfo, String action) throws Exception {
        if (communicationsBulkRequestInfo !=null && communicationsBulkRequestInfo.filter!=null) {
            Optional<DestinationSpec> destinationSpec = messageService.getDestinationSpec(CommunicationTaskService.FILTER_ITEMIZER_QUEUE_DESTINATION);
            if (destinationSpec.isPresent()) {
                return processMessagePost(new ItemizeCommunicationsFilterQueueMessage(substituteRestToDomainEnums(communicationsBulkRequestInfo.filter), action), destinationSpec.get());
            } else {
                throw exceptionFactory.newException(MessageSeeds.NO_SUCH_MESSAGE_QUEUE);
            }

        } else if (communicationsBulkRequestInfo !=null && communicationsBulkRequestInfo.communications!=null) {
            Optional<DestinationSpec> destinationSpec = messageService.getDestinationSpec(CommunicationTaskService.COMMUNICATION_RESCHEDULER_QUEUE_DESTINATION);
            if (destinationSpec.isPresent()) {
                communicationsBulkRequestInfo.communications.stream().forEach(c -> processMessagePost(new ComTaskExecutionQueueMessage(c, action), destinationSpec.get()));
                return Response.status(Response.Status.OK).build();
            } else {
                throw exceptionFactory.newException(MessageSeeds.NO_SUCH_MESSAGE_QUEUE);
            }
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    private boolean verifyAppServerExists(String destinationName) {
        return appService.findAppServers().stream().
                filter(AppServer::isActive).
                flatMap(server->server.getSubscriberExecutionSpecs().stream()).
                map(execSpec->execSpec.getSubscriberSpec().getDestination()).
                filter(DestinationSpec::isActive).
                filter(spec -> !spec.getSubscribers().isEmpty()).
                anyMatch(spec -> destinationName.equals(spec.getName()));
    }

    private ComTaskExecutionFilterSpecificationMessage substituteRestToDomainEnums(ComTaskExecutionFilterSpecificationMessage jsonQueryFilter) throws Exception {
        if (jsonQueryFilter.currentStates!=null) {
            jsonQueryFilter.currentStates =
                    jsonQueryFilter.currentStates
                            .stream()
                            .map(TaskStatus::valueOf)
                            .map(TaskStatus::name)
                            .collect(toSet());
        }

        if (jsonQueryFilter.latestResults!=null) {
            jsonQueryFilter.latestResults =
                    jsonQueryFilter.latestResults
                            .stream()
                            .map(CompletionCode::valueOf)
                            .map(Enum::name)
                            .collect(toSet());
        }

        return jsonQueryFilter;
    }


    private ComTaskExecutionFilterSpecification buildFilterFromJsonQuery(JsonQueryFilter jsonQueryFilter) throws Exception {
        ComTaskExecutionFilterSpecification filter = new ComTaskExecutionFilterSpecification();

        filter.taskStatuses = EnumSet.noneOf(TaskStatus.class);
        if (jsonQueryFilter.hasProperty(FilterOption.currentStates.name())) {
            List<TaskStatus> taskStatuses = jsonQueryFilter.getStringList(FilterOption.currentStates.name()).stream().map(TaskStatus::valueOf).collect(Collectors.toList());
            filter.taskStatuses.addAll(taskStatuses);
        }

        filter.latestResults = EnumSet.noneOf(CompletionCode.class);
        if (jsonQueryFilter.hasProperty(FilterOption.latestResults.name())) {
            List<CompletionCode> latestResults = jsonQueryFilter.getStringList(FilterOption.latestResults.name()).stream().map(CompletionCode::valueOf).collect(Collectors.toList());
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
            filter.comTasks.addAll(getObjectsByIdFromList(comTaskIds, taskService.findAllComTasks().find()));
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

    private Response processMessagePost(QueueMessage message, DestinationSpec destinationSpec) {
        String json = jsonService.serialize(message);
        destinationSpec.message(json).send();
        return Response.ok().entity("{\"success\":\"true\"}").build();
    }

}