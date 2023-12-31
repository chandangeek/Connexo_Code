/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.PartialConnectionTask;
import com.energyict.mdc.common.tasks.TaskStatus;
import com.energyict.mdc.common.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.common.tasks.history.CompletionCode;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.QueueMessage;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFilterSpecification;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFilterSpecificationMessage;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionQueueMessage;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.ItemizeCommunicationsFilterQueueMessage;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.TaskService;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Functions.asStream;
import static com.energyict.mdc.dashboard.rest.status.impl.TranslationKeys.NEVER_STARTED_COMPLETION_CODE;
import static java.util.stream.Collectors.toSet;

@Path("/communications")
public class CommunicationResource {
    private static final int MAX_RECORDS_PER_PAGE = 1000;
    private static final Logger LOGGER = Logger.getLogger(CommunicationResource.class.getName());

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
    private final ProtocolPluggableService protocolPluggableService;
    private final ResourceHelper resourceHelper;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final ConnectionTaskService connectionTaskService;

    @Inject
    public CommunicationResource(CommunicationTaskService communicationTaskService, SchedulingService schedulingService, DeviceConfigurationService deviceConfigurationService, TaskService taskService, ComTaskExecutionInfoFactory comTaskExecutionInfoFactory, MeteringGroupsService meteringGroupsService, ExceptionFactory exceptionFactory, JsonService jsonService, AppService appService, MessageService messageService, ResourceHelper resourceHelper, ConcurrentModificationExceptionFactory conflictFactory, ProtocolPluggableService protocolPluggableService,
                                 ConnectionTaskService connectionTaskService) {
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
        this.protocolPluggableService = protocolPluggableService;
        this.resourceHelper = resourceHelper;
        this.conflictFactory = conflictFactory;
        this.connectionTaskService = connectionTaskService;
    }

    @GET
    @Transactional
    @Consumes("application/json")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response getCommunications(@BeanParam JsonQueryFilter jsonQueryFilter, @BeanParam JsonQueryParameters queryParameters, @Context SecurityContext securityContext) throws Exception {
        ComTaskExecutionFilterSpecification filter = buildFilterFromJsonQuery(jsonQueryFilter);
        String user = "";
        if (null != securityContext) {
            user = securityContext.getUserPrincipal().getName();
        }
        LOGGER.log(Level.INFO, "CONM-3550: Get Com tasks filter applied by " + user + " with filter properties:: comTasks: " + jsonQueryFilter.getComplexProperty("comTasks") + ", device: " + jsonQueryFilter.getComplexProperty("device") + ", connectionMethods: " + jsonQueryFilter.getComplexProperty("connectionMethods") + ", deviceTypes: " + jsonQueryFilter.getComplexProperty("deviceTypes") + ", deviceGroups: " + jsonQueryFilter.getComplexProperty("deviceGroups") + ", latestResults: " + jsonQueryFilter.getComplexProperty("latestResults") + ", currentStates: " + jsonQueryFilter.getComplexProperty("currentStates") + ", comSchedules: " + jsonQueryFilter.getComplexProperty("comSchedules") + ", startIntervalFrom: " + jsonQueryFilter.getComplexProperty("startIntervalFrom") + ", startIntervalTo: " + jsonQueryFilter.getComplexProperty("startIntervalTo") + ", finishIntervalFrom: " + jsonQueryFilter.getComplexProperty("finishIntervalFrom") + ", finishIntervalTo: " + jsonQueryFilter.getComplexProperty("finishIntervalTo"));
        if (!queryParameters.getStart().isPresent() || !queryParameters.getLimit().isPresent()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        try {
            List<ComTaskExecutionInfo> comTaskExecutionInfos;
            List<ComTaskExecution> communicationTasks = communicationTaskService.findComTaskExecutionsByFilter(filter, queryParameters.getStart().get(), queryParameters.getLimit().get() + 1);
            if (queryParameters.getLimit().get() > MAX_RECORDS_PER_PAGE) {
                comTaskExecutionInfos =
                        communicationTasks
                                .stream()
                                .map(comTaskExecutionInfoFactory::getExportInfo)
                                .collect(Collectors.toList());
            } else {
                comTaskExecutionInfos =
                        communicationTasks
                                .stream()
                                .map(this::toComTaskExecutionInfo)
                                .collect(Collectors.toList());
            }
            return Response.ok(PagedInfoList.fromPagedList("communicationTasks", comTaskExecutionInfos, queryParameters)).build();

        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(
                    "{\"success\": false, \"message\": \"" + e.getMessage() + "\",\"errors\": [{\"message\": \"" + e.getMessage() + "\"}]}").build());
        }
    }

    private ComTaskExecutionInfo toComTaskExecutionInfo(ComTaskExecution comTaskExecution) {
        Optional<ComTaskExecutionSession> lastComTaskExecutionSession = this.communicationTaskService.findLastSessionFor(comTaskExecution);
        return this.comTaskExecutionInfoFactory.from(comTaskExecution, lastComTaskExecutionSession);
    }

    @GET
    @Transactional
    @Consumes("application/json")
    @Path("/count")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response getCommunicationsCount(@BeanParam JsonQueryFilter jsonQueryFilter, @BeanParam JsonQueryParameters queryParameters, @Context SecurityContext securityContext) throws Exception {
        String user = "";
        if (null != securityContext) {
            user = securityContext.getUserPrincipal().getName();
        }
        LOGGER.log(Level.INFO, "CONM-3550: Get count of Com tasks by " + user + " with filter properties:: comTasks: " + jsonQueryFilter.getComplexProperty("comTasks") + ", device: " + jsonQueryFilter.getComplexProperty("device") + ", connectionMethods: " + jsonQueryFilter.getComplexProperty("connectionMethods") + ", deviceTypes: " + jsonQueryFilter.getComplexProperty("deviceTypes") + ", deviceGroups: " + jsonQueryFilter.getComplexProperty("deviceGroups") + ", latestResults: " + jsonQueryFilter.getComplexProperty("latestResults") + ", currentStates: " + jsonQueryFilter.getComplexProperty("currentStates") + ", comSchedules: " + jsonQueryFilter.getComplexProperty("comSchedules") + ", startIntervalFrom: " + jsonQueryFilter.getComplexProperty("startIntervalFrom") + ", startIntervalTo: " + jsonQueryFilter.getComplexProperty("startIntervalTo") + ", finishIntervalFrom: " + jsonQueryFilter.getComplexProperty("finishIntervalFrom") + ", finishIntervalTo: " + jsonQueryFilter.getComplexProperty("finishIntervalTo"));
        ComTaskExecutionFilterSpecification filter = buildFilterFromJsonQuery(jsonQueryFilter);
        return Response.ok(communicationTaskService.getCommunicationTasksCount(filter)).build();
    }

    @PUT
    @Transactional
    @Path("/{comTaskExecId}/run")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response runCommunication(@PathParam("comTaskExecId") long comTaskExecId, ComTaskExecutionInfo info) {
        info.id = comTaskExecId;
        lockConnectionTask(comTaskExecId);
        ComTaskExecution comTaskExecution = resourceHelper.getLockedComTaskExecution(info.id, info.version)
                .orElseThrow(conflictFactory.conflict()
                        .withActualVersion(() -> resourceHelper.getCurrentComTaskExecutionVersion(info.id))
                        .withMessageTitle(MessageSeeds.CONCURRENT_RUN_TITLE, info.name)
                        .withMessageBody(MessageSeeds.CONCURRENT_RUN_BODY, info.name)
                        .supplier());
        comTaskExecution.scheduleNow();
        return Response.status(Response.Status.OK).build();
    }

    @PUT
    @Transactional
    @Path("/{comTaskExecId}/runnow")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response runCommunicationNow(@PathParam("comTaskExecId") long comTaskExecId, ComTaskExecutionInfo info) {
        info.id = comTaskExecId;
        lockConnectionTask(comTaskExecId);
        ComTaskExecution comTaskExecution = resourceHelper.getLockedComTaskExecution(info.id, info.version)
                .orElseThrow(conflictFactory.conflict()
                        .withActualVersion(() -> resourceHelper.getCurrentComTaskExecutionVersion(info.id))
                        .withMessageTitle(MessageSeeds.CONCURRENT_RUN_TITLE, info.name)
                        .withMessageBody(MessageSeeds.CONCURRENT_RUN_BODY, info.name)
                        .supplier());
        comTaskExecution.runNow();
        return Response.status(Response.Status.OK).build();
    }

    private void lockConnectionTask(long comTaskExecId) {
        ComTaskExecution comTaskExecution = communicationTaskService.findComTaskExecution(comTaskExecId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_COMMUNICATION_TASK, comTaskExecId));
        resourceHelper.getLockedConnectionTask(comTaskExecution.getConnectionTaskId())
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_CONNECTION_TASK, comTaskExecution.getConnectionTaskId()));
    }

    @PUT
    @Transactional
    @Path("/{comTaskExecId}/runprio")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response runCommunicationWithPriority(@PathParam("comTaskExecId") long comTaskExecId, ComTaskExecutionInfo info) {
        info.id = comTaskExecId;
        lockConnectionTask(comTaskExecId);
        ComTaskExecution comTaskExecution = resourceHelper.getLockedComTaskExecution(info.id, info.version)
                .orElseThrow(conflictFactory.conflict()
                        .withActualVersion(() -> resourceHelper.getCurrentComTaskExecutionVersion(info.id))
                        .withMessageTitle(MessageSeeds.CONCURRENT_RUN_TITLE, info.name)
                        .withMessageBody(MessageSeeds.CONCURRENT_RUN_BODY, info.name)
                        .supplier());
        resourceHelper.getPriorityComTaskExecution(comTaskExecution);
        comTaskExecution.runNow();
        return Response.status(Response.Status.OK).build();
    }

    @PUT
    @Transactional
    @Path("/run")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response runCommunicationTask(CommunicationsBulkRequestInfo communicationsBulkRequestInfo, @Context SecurityContext securityContext) throws Exception {
        if (!verifyAppServerExists(CommunicationTaskService.FILTER_ITEMIZER_QUEUE_DESTINATION) || !verifyAppServerExists(CommunicationTaskService.COMMUNICATION_RESCHEDULER_QUEUE_DESTINATION)) {
            throw exceptionFactory.newException(MessageSeeds.NO_APPSERVER);
        }
        String user = "";
        if (null != securityContext) {
            user = securityContext.getUserPrincipal().getName();
        }
        return queueCommunicationBulkAction(communicationsBulkRequestInfo, "scheduleNow", user);
    }

    @PUT
    @Transactional
    @Path("/runnow")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response runCommunicationTaskNow(CommunicationsBulkRequestInfo communicationsBulkRequestInfo, @Context SecurityContext securityContext) throws Exception {
        if (!verifyAppServerExists(CommunicationTaskService.FILTER_ITEMIZER_QUEUE_DESTINATION) || !verifyAppServerExists(CommunicationTaskService.COMMUNICATION_RESCHEDULER_QUEUE_DESTINATION)) {
            throw exceptionFactory.newException(MessageSeeds.NO_APPSERVER);
        }
        String user = "";
        if (null != securityContext) {
            user = securityContext.getUserPrincipal().getName();
        }
        return queueCommunicationBulkAction(communicationsBulkRequestInfo, "runNow", user);
    }

    private Response queueCommunicationBulkAction(CommunicationsBulkRequestInfo communicationsBulkRequestInfo, String action, String user) throws Exception {
        if (communicationsBulkRequestInfo.filter != null) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValueAsString(communicationsBulkRequestInfo.filter);
            try {
                LOGGER.log(Level.INFO, "CONM-3550: Bulk Action Filter by " + user + " with filter properties: " + new ObjectMapper().writeValueAsString(communicationsBulkRequestInfo.filter) + " with the action : " + action);
            } catch (Exception e) {
                //ignore if any failure
            }
        } else {
            LOGGER.log(Level.INFO, "CONM-3550: Number of communication tasks selected by " + user + " are: " + communicationsBulkRequestInfo.communications.size());
        }
        if (communicationsBulkRequestInfo.filter != null) {
            if (communicationsBulkRequestInfo.filter.device == null
                    && (communicationsBulkRequestInfo.filter.deviceTypes == null
                    || communicationsBulkRequestInfo.filter.deviceTypes.isEmpty())
                    && (communicationsBulkRequestInfo.filter.deviceGroups == null
                    || communicationsBulkRequestInfo.filter.deviceGroups.isEmpty())
                    && (communicationsBulkRequestInfo.filter.comTasks == null
                    || communicationsBulkRequestInfo.filter.comTasks.isEmpty())
                    && (communicationsBulkRequestInfo.filter.comSchedules == null
                    || communicationsBulkRequestInfo.filter.comSchedules.isEmpty())
                    && (communicationsBulkRequestInfo.filter.currentStates == null
                    || communicationsBulkRequestInfo.filter.currentStates.isEmpty())
                    && (communicationsBulkRequestInfo.filter.latestResults == null
                    || communicationsBulkRequestInfo.filter.latestResults.isEmpty())
                    && (communicationsBulkRequestInfo.filter.connectionMethods == null
                    || communicationsBulkRequestInfo.filter.connectionMethods.isEmpty())
                    && communicationsBulkRequestInfo.filter.startIntervalFrom == null
                    && communicationsBulkRequestInfo.filter.startIntervalTo == null
                    && communicationsBulkRequestInfo.filter.finishIntervalFrom == null
                    && communicationsBulkRequestInfo.filter.finishIntervalTo == null) {
                LOGGER.log(Level.WARNING, "CONM-3550: User " + user + " triggered bulk action of communication tasks with action " + action + " without applying any filters. Therefore, the comtasks are not queued.");
                throw exceptionFactory.newException(MessageSeeds.UNSUPPORTED_ACTION);
            }
            Optional<DestinationSpec> destinationSpec = messageService.getDestinationSpec(CommunicationTaskService.FILTER_ITEMIZER_QUEUE_DESTINATION);
            if (destinationSpec.isPresent()) {
                return processMessagePost(new ItemizeCommunicationsFilterQueueMessage(substituteRestToDomainEnums(communicationsBulkRequestInfo.filter), action), destinationSpec.get());
            } else {
                throw exceptionFactory.newException(MessageSeeds.NO_SUCH_MESSAGE_QUEUE);
            }

        } else if (communicationsBulkRequestInfo.communications != null) {
            Optional<DestinationSpec> destinationSpec = messageService.getDestinationSpec(CommunicationTaskService.COMMUNICATION_RESCHEDULER_QUEUE_DESTINATION);
            if (destinationSpec.isPresent()) {
                communicationsBulkRequestInfo.communications.forEach(c -> processMessagePost(new ComTaskExecutionQueueMessage(c, action), destinationSpec.get()));
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
                flatMap(server -> server.getSubscriberExecutionSpecs().stream()).
                map(execSpec -> execSpec.getSubscriberSpec().getDestination()).
                filter(DestinationSpec::isActive).
                filter(spec -> !spec.getSubscribers().isEmpty()).
                anyMatch(spec -> destinationName.equals(spec.getName()));
    }

    private ComTaskExecutionFilterSpecificationMessage substituteRestToDomainEnums(ComTaskExecutionFilterSpecificationMessage jsonQueryFilter) throws Exception {
        if (jsonQueryFilter.currentStates != null) {
            jsonQueryFilter.currentStates =
                    jsonQueryFilter.currentStates
                            .stream()
                            .map(TaskStatus::valueOf)
                            .map(TaskStatus::name)
                            .collect(toSet());
        }

        if (jsonQueryFilter.latestResults != null) {
            jsonQueryFilter.latestResults =
                    jsonQueryFilter.latestResults
                            .stream()
                            .map(result -> result.equals(NEVER_STARTED_COMPLETION_CODE.getKey()) ? null : CompletionCode.valueOf(result).name())
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

        filter.latestResults = new HashSet<>();
        if (jsonQueryFilter.hasProperty(FilterOption.latestResults.name())) {
            List<CompletionCode> latestResults = jsonQueryFilter.getStringList(FilterOption.latestResults.name()).stream().map(CommunicationResource::completionCodeFrom).collect(Collectors.toList());
            filter.latestResults.addAll(latestResults);
        }

        filter.comSchedules = new HashSet<>();
        if (jsonQueryFilter.hasProperty(FilterOption.comSchedules.name())) {
            List<Long> comScheduleIds = jsonQueryFilter.getLongList(FilterOption.comSchedules.name());
            filter.comSchedules.addAll(getObjectsByIdFromList(comScheduleIds, schedulingService.getAllSchedules()));
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
            jsonQueryFilter.getLongList(FilterOption.deviceGroups.name()).forEach(id -> filter.deviceGroups.add(meteringGroupsService.findEndDeviceGroup(id).get()));
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

        if (jsonQueryFilter.hasProperty(HeatMapBreakdownOption.connectionTypes.name())) {
            List<Long> connectionTypeIds = jsonQueryFilter.getLongList(FilterOption.connectionTypes.name());
            filter.connectionTypes = connectionTypeIds
                    .stream()
                    .map(protocolPluggableService::findConnectionTypePluggableClass)
                    .flatMap(asStream())
                    .collect(toSet());
        }

        if (jsonQueryFilter.hasProperty("device")) {
            filter.deviceName = jsonQueryFilter.getString("device");
        }

//        if (jsonQueryFilter.hasProperty(FilterOption.connectionMethods.name())) {
//            filter.connectionMethods = jsonQueryFilter.getLongList(FilterOption.connectionMethods.name());
//        }
        if (jsonQueryFilter.hasProperty(FilterOption.connectionMethods.name())) {

            List<String> connectionMethods = jsonQueryFilter.getStringList(FilterOption.connectionMethods.name());

            List<PartialConnectionTask> partialConnectionTasks = connectionTaskService.findPartialConnectionTasks();

            filter.connectionMethods = partialConnectionTasks
                    .stream()
                    .distinct()
                    .filter(pct -> connectionMethods.contains(pct.getName()))
                    .map(PartialConnectionTask::getId)
                    .collect(Collectors.toList());
        }

        if (jsonQueryFilter.hasProperty(FilterOption.location.name())) {
            filter.locationId = jsonQueryFilter.getLong(FilterOption.location.name());
        }

        filter.showSlaveComTaskExecutions = true;
        return filter;
    }

    private static CompletionCode completionCodeFrom(String name) {
        return name.equals(NEVER_STARTED_COMPLETION_CODE.getKey()) ? null : CompletionCode.valueOf(name);
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
