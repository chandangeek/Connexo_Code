package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.FilterFactory;
import com.energyict.mdc.device.data.QueueMessage;
import com.energyict.mdc.device.data.rest.ComSessionSuccessIndicatorAdapter;
import com.energyict.mdc.device.data.rest.ConnectionTaskSuccessIndicatorAdapter;
import com.energyict.mdc.device.data.rest.TaskStatusAdapter;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFilterSpecification;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFilterSpecificationMessage;
import com.energyict.mdc.device.data.tasks.RescheduleConnectionTaskQueueMessage;
import com.energyict.mdc.device.data.tasks.ItemizeConnectionFilterRescheduleQueueMessage;
import com.energyict.mdc.device.data.tasks.ItemizeConnectionFilterUpdatePropertiesQueueMessage;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.UpdateConnectionTaskPropertiesQueueMessage;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static com.elster.jupiter.util.streams.Functions.asStream;
import static java.util.stream.Collectors.toSet;

@Path("/connections")
public class ConnectionResource {

    private static final TaskStatusAdapter TASK_STATUS_ADAPTER = new TaskStatusAdapter();
    private static final ComSessionSuccessIndicatorAdapter COM_SESSION_SUCCESS_INDICATOR_ADAPTER = new ComSessionSuccessIndicatorAdapter();
    private static final ConnectionTaskSuccessIndicatorAdapter CONNECTION_TASK_SUCCESS_INDICATOR_ADAPTER = new ConnectionTaskSuccessIndicatorAdapter();

    private final ConnectionTaskService connectionTaskService;
    private final EngineConfigurationService engineConfigurationService;
    private final ProtocolPluggableService protocolPluggableService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final ConnectionTaskInfoFactory connectionTaskInfoFactory;
    private final ExceptionFactory exceptionFactory;
    private final MeteringGroupsService meteringGroupsService;
    private final ComTaskExecutionSessionInfoFactory comTaskExecutionSessionInfoFactory;
    private final MessageService messageService;
    private final JsonService jsonService;
    private final AppService appService;
    private final MdcPropertyUtils mdcPropertyUtils;
    private final FilterFactory filterFactory;

    @Inject
    public ConnectionResource(ConnectionTaskService connectionTaskService, EngineConfigurationService engineConfigurationService, ProtocolPluggableService protocolPluggableService, DeviceConfigurationService deviceConfigurationService, ConnectionTaskInfoFactory connectionTaskInfoFactory, ExceptionFactory exceptionFactory, MeteringGroupsService meteringGroupsService, ComTaskExecutionSessionInfoFactory comTaskExecutionSessionInfoFactory, MessageService messageService, JsonService jsonService, AppService appService, MdcPropertyUtils mdcPropertyUtils, FilterFactory filterFactory) {
        super();
        this.connectionTaskService = connectionTaskService;
        this.engineConfigurationService = engineConfigurationService;
        this.protocolPluggableService = protocolPluggableService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.connectionTaskInfoFactory = connectionTaskInfoFactory;
        this.exceptionFactory = exceptionFactory;
        this.meteringGroupsService = meteringGroupsService;
        this.comTaskExecutionSessionInfoFactory = comTaskExecutionSessionInfoFactory;
        this.messageService = messageService;
        this.jsonService = jsonService;
        this.appService = appService;
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.filterFactory = filterFactory;
    }

    @GET
    @Path("/connectiontypepluggableclasses")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
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
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response getConnections(@BeanParam JsonQueryFilter jsonQueryFilter, @BeanParam JsonQueryParameters queryParameters) throws Exception {
        ConnectionTaskFilterSpecification filter = buildFilterFromJsonQuery(jsonQueryFilter);
        if (!queryParameters.getStart().isPresent() || !queryParameters.getLimit().isPresent()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        List<ConnectionTask> connectionTasksByFilter = connectionTaskService.findConnectionTasksByFilter(filter, queryParameters.getStart().get(), queryParameters.getLimit().get() + 1);
        List<ConnectionTaskInfo> connectionTaskInfos = new ArrayList<>(connectionTasksByFilter.size());
        for (ConnectionTask<?, ?> connectionTask : connectionTasksByFilter) {
            Optional<ComSession> lastComSession = connectionTask.getLastComSession();
            connectionTaskInfos.add(connectionTaskInfoFactory.from(connectionTask, lastComSession));
        }
        return Response.ok(PagedInfoList.fromPagedList("connectionTasks", connectionTaskInfos, queryParameters)).build();
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
            for (ComPortPool comPortPool : engineConfigurationService.findAllComPortPools()) {
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
                    .flatMap(asStream())
                    .collect(toSet());
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
                            .flatMap(asStream())
                            .collect(Collectors.toList()));
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

        if (jsonQueryFilter.hasProperty(FilterOption.deviceGroups.name())) {
            filter.deviceGroups = new HashSet<>();
            jsonQueryFilter.getLongList(FilterOption.deviceGroups.name()).stream().forEach(id -> filter.deviceGroups.add(meteringGroupsService.findEndDeviceGroup(id).get()));
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

    private ConnectionTaskFilterSpecificationMessage substituteRestToDomainEnums(ConnectionTaskFilterSpecificationMessage jsonQueryFilter) throws Exception {
        if (jsonQueryFilter.currentStates!=null) {
            jsonQueryFilter.currentStates=jsonQueryFilter.currentStates.stream().map(TASK_STATUS_ADAPTER::unmarshal).map(Enum::name).collect(toSet());
        }

        if (jsonQueryFilter.latestResults!=null) {
            jsonQueryFilter.latestResults=jsonQueryFilter.latestResults.stream().map(COM_SESSION_SUCCESS_INDICATOR_ADAPTER::unmarshal).map(Enum::name).collect(toSet());
        }

        if (jsonQueryFilter.latestStates!=null) {
            jsonQueryFilter.latestStates=jsonQueryFilter.latestStates.stream().map(CONNECTION_TASK_SUCCESS_INDICATOR_ADAPTER::unmarshal).map(Enum::name).collect(toSet());
        }

        return jsonQueryFilter;
    }

    @GET
    @Path("/{connectionId}/latestcommunications")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public PagedInfoList getCommunications(@PathParam("connectionId") long connectionId, @BeanParam JsonQueryParameters queryParameters) {
        ConnectionTask connectionTask =
                connectionTaskService
                        .findConnectionTask(connectionId)
                        .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_CONNECTION_TASK, connectionId));
        Optional<ComSession> lastComSessionOptional = connectionTask.getLastComSession();
        List<ComTaskExecutionSession> comTaskExecutionSessions = new ArrayList<>();
        if (lastComSessionOptional.isPresent()) {
            comTaskExecutionSessions.addAll(lastComSessionOptional.get().getComTaskExecutionSessions());
        }

        return PagedInfoList.fromPagedList("communications", comTaskExecutionSessionInfoFactory.from(comTaskExecutionSessions), queryParameters);
    }

    @PUT
    @Path("/{connectionId}/run")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    // TODO Would be better if this method moved to ConnectionResource in device.data.rest
    public Response runConnectionTask(@PathParam("connectionId") long connectionId, @Context UriInfo uriInfo) {
        ConnectionTask connectionTask =
                connectionTaskService
                        .findConnectionTask(connectionId)
                        .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_CONNECTION_TASK, connectionId));

        if (connectionTask instanceof ScheduledConnectionTask) {
            ScheduledConnectionTask scheduledConnectionTask = (ScheduledConnectionTask) connectionTask;
            scheduledConnectionTask.getScheduledComTasks().stream().
                    filter(comTaskExecution -> EnumSet.of(TaskStatus.Failed, TaskStatus.Retrying, TaskStatus.NeverCompleted, TaskStatus.Pending).contains(comTaskExecution.getStatus())).
                    filter(comTaskExecution -> !comTaskExecution.isObsolete()).
                    forEach(ComTaskExecution::runNow);

            scheduledConnectionTask.scheduleNow();
        } else {
            throw exceptionFactory.newException(MessageSeeds.RUN_CONNECTIONTASK_IMPOSSIBLE);
        }
        return Response.status(Response.Status.OK).build();
    }

    @PUT
    @Path("/run")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response runConnectionTask(ConnectionsBulkRequestInfo connectionsBulkRequestInfo) throws Exception {
        if (!verifyAppServerExists(ConnectionTaskService.FILTER_ITEMIZER_QUEUE_DESTINATION) || !verifyAppServerExists(ConnectionTaskService.CONNECTION_RESCHEDULER_QUEUE_DESTINATION)) {
            throw exceptionFactory.newException(MessageSeeds.NO_APPSERVER);
        }
        if (connectionsBulkRequestInfo !=null && connectionsBulkRequestInfo.filter!=null) {
            Optional<DestinationSpec> destinationSpec = messageService.getDestinationSpec(ConnectionTaskService.FILTER_ITEMIZER_QUEUE_DESTINATION);
            if (destinationSpec.isPresent()) {
                return processMessagePost(new ItemizeConnectionFilterRescheduleQueueMessage(substituteRestToDomainEnums(connectionsBulkRequestInfo.filter), "scheduleNow"), destinationSpec.get());
            } else {
                throw exceptionFactory.newException(MessageSeeds.NO_SUCH_MESSAGE_QUEUE);
            }

        } else if (connectionsBulkRequestInfo !=null && connectionsBulkRequestInfo.connections!=null) {
            Optional<DestinationSpec> destinationSpec = messageService.getDestinationSpec(ConnectionTaskService.CONNECTION_RESCHEDULER_QUEUE_DESTINATION);
            if (destinationSpec.isPresent()) {
                connectionsBulkRequestInfo.connections.stream().forEach(c -> processMessagePost(new RescheduleConnectionTaskQueueMessage(c, "scheduleNow"), destinationSpec.get()));
                return Response.status(Response.Status.OK).build();
            } else {
                throw exceptionFactory.newException(MessageSeeds.NO_SUCH_MESSAGE_QUEUE);
            }
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @GET
    @Path("/properties")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response getCommonConnectionTaskProperties(@QueryParam("filter") String filterQueryParam, @QueryParam("connections") String connectionsQueryParam) throws Exception {
        ConnectionTypePluggableClass connectionType = getConnectionTypePluggableClassFromQueryParameters(filterQueryParam, connectionsQueryParam);
        List<PropertySpec> propertySpecs = connectionType.getPropertySpecs();
        TypedProperties typedProperties = connectionType.getProperties(propertySpecs);
        PropertiesBulkRequestInfo info = new PropertiesBulkRequestInfo();
        info.properties=mdcPropertyUtils.convertPropertySpecsToPropertyInfos(propertySpecs, typedProperties);
        return Response.ok(info).build();
    }

    @PUT
    @Path("/properties")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response updateCommonConnectionTaskProperties(PropertiesBulkRequestInfo propertiesBulkRequestInfo) throws Exception {
        if (!verifyAppServerExists(ConnectionTaskService.FILTER_ITEMIZER_PROPERTIES_QUEUE_DESTINATION) || !verifyAppServerExists(ConnectionTaskService.CONNECTION_PROP_UPDATER_QUEUE_DESTINATION)) {
            throw exceptionFactory.newException(MessageSeeds.NO_APPSERVER);
        }
        if (propertiesBulkRequestInfo!=null && propertiesBulkRequestInfo.properties!=null && !propertiesBulkRequestInfo.properties.isEmpty()) {
            // we do the call below merely as validation for uniqueness of ConnectionTypePluggableClass
            getConnectionTypePluggableClassFromQueryParameters(propertiesBulkRequestInfo.filter, propertiesBulkRequestInfo.connections);
            Map<String, String> properties = convertPropertyInfosToMap(propertiesBulkRequestInfo.properties);

            if (propertiesBulkRequestInfo.filter != null) {
                Optional<DestinationSpec> destinationSpec = messageService.getDestinationSpec(ConnectionTaskService.FILTER_ITEMIZER_PROPERTIES_QUEUE_DESTINATION);
                if (destinationSpec.isPresent()) {
                    return processMessagePost(new ItemizeConnectionFilterUpdatePropertiesQueueMessage(substituteRestToDomainEnums(propertiesBulkRequestInfo.filter), properties), destinationSpec.get());
                } else {
                    throw exceptionFactory.newException(MessageSeeds.NO_SUCH_MESSAGE_QUEUE);
                }

            } else if (propertiesBulkRequestInfo.connections != null) {
                Optional<DestinationSpec> destinationSpec = messageService.getDestinationSpec(ConnectionTaskService.CONNECTION_PROP_UPDATER_QUEUE_DESTINATION);
                if (destinationSpec.isPresent()) {
                    propertiesBulkRequestInfo.connections.stream().forEach(c -> processMessagePost(new UpdateConnectionTaskPropertiesQueueMessage(c, properties), destinationSpec.get()));
                    return Response.status(Response.Status.OK).build();
                } else {
                    throw exceptionFactory.newException(MessageSeeds.NO_SUCH_MESSAGE_QUEUE);
                }
            }
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    private Map<String, String> convertPropertyInfosToMap(List<PropertyInfo> propertyInfos) {
        Map<String, String> properties = new HashMap<>();
        propertyInfos.stream().forEach(info -> {
            Object value = info.getPropertyValueInfo().getValue();
            properties.put(info.key, value==null?null:jsonService.serialize(value));
        });
        return properties;
    }

    /**
     * Gets the ConnectionTypePluggableClass from either filter or list of connections
     * @param filterQueryParam Describes the filter accepted by the method
     * @param connectionsQueryParam Describes a list of (long) ids
     * @return unique ConnectionTypePluggableClass, or exception if not unique
     * @throws IOException if Jackson was unable to parse the Json filter or id list
     * @throws RuntimeException if the distills ConnectionTypePluggableClass was not unique
     *
     */
    private ConnectionTypePluggableClass getConnectionTypePluggableClassFromQueryParameters(String filterQueryParam, String connectionsQueryParam) throws Exception {
        List<ConnectionTypePluggableClass> connectionTypePluggableClasses = new ArrayList<>();
        if (filterQueryParam!=null) {
            ConnectionTaskFilterSpecification filter = buildFilterFromJsonQuery(new JsonQueryFilter(filterQueryParam));
            connectionTypePluggableClasses.addAll(connectionTaskService.findConnectionTypeByFilter(filter));
        } else if (connectionsQueryParam!=null) {
            connectionTypePluggableClasses.addAll(getPluggableClassesFromConnectionQueryString(connectionsQueryParam));
        }

        validateConnectionTypeSelection(connectionTypePluggableClasses);
        return connectionTypePluggableClasses.get(0);
    }

    /**
     * Gets the ConnectionTypePluggableClass from either filter or list of connections
     * @param filterMessage Describes the filter accepted by the method
     * @param connections Describes a list of (long) ids
     * @return unique ConnectionTypePluggableClass, or exception if not unique
     * @throws IOException if Jackson was unable to parse the Json filter or id list
     * @throws RuntimeException if the distilles ConnectionTypePluggableClass was not unique
     *
     */
    private ConnectionTypePluggableClass getConnectionTypePluggableClassFromQueryParameters(ConnectionTaskFilterSpecificationMessage filterMessage, List<Long> connections) throws Exception {
        List<ConnectionTypePluggableClass> connectionTypePluggableClasses = new ArrayList<>();
        if (filterMessage!=null) {
            ConnectionTaskFilterSpecification filter = filterFactory.buildFilterFromMessage(filterMessage);
            connectionTypePluggableClasses.addAll(connectionTaskService.findConnectionTypeByFilter(filter));
        } else if (connections!=null) {
            connectionTypePluggableClasses.addAll(getPluggableClassesFromConnectionTaskIdList(connections));
        }

        validateConnectionTypeSelection(connectionTypePluggableClasses);
        return connectionTypePluggableClasses.get(0);
    }

    private void validateConnectionTypeSelection(List<ConnectionTypePluggableClass> connectionTypePluggableClasses) {
        if (connectionTypePluggableClasses.size()>1) {
            throw exceptionFactory.newException(MessageSeeds.CONNECTION_TASK_NOT_UNIQUE);
        }
        if (connectionTypePluggableClasses.isEmpty()) {
            throw exceptionFactory.newException(MessageSeeds.ONE_CONNECTION_TYPE_REQUIRED);
        }
    }

    private List<ConnectionTypePluggableClass> getPluggableClassesFromConnectionQueryString(String connectionsQueryParam) throws IOException {
        List<ConnectionTypePluggableClass> connectionTypePluggableClasses = new ArrayList<>();
        JsonNode node = new ObjectMapper().readValue(connectionsQueryParam, JsonNode.class);
        if (node != null && node.isArray()) {
            List<Long> connectionTaskIds = new ArrayList<>();
            for (JsonNode singleFilter : node) {
                connectionTaskIds.add(singleFilter.asLong());
            }
            connectionTypePluggableClasses.addAll(getPluggableClassesFromConnectionTaskIdList(connectionTaskIds));
        }
        return connectionTypePluggableClasses;
    }

    private List<ConnectionTypePluggableClass> getPluggableClassesFromConnectionTaskIdList(List<Long> connections) throws IOException {
        List<ConnectionTypePluggableClass> connectionTypePluggableClasses = new ArrayList<>();
        if (connections != null) {
            List<String> javaClassNames = new ArrayList<>();
            for (Long connectionTaskId : connections) {
                connectionTaskService.
                        findConnectionTask(connectionTaskId).
                        ifPresent((connectionTask) -> javaClassNames.add(connectionTask.getPluggableClass().getJavaClassName()));
            }
            protocolPluggableService.findAllConnectionTypePluggableClasses().stream().
                    filter(pluggableClass->javaClassNames.contains(pluggableClass.getJavaClassName())).
                    forEach(connectionTypePluggableClasses::add);
        }
        return connectionTypePluggableClasses;
    }

    private boolean verifyAppServerExists(String destinationName) {
        return appService.findAppServers().stream().
                filter(AppServer::isActive).
                flatMap(server->server.getSubscriberExecutionSpecs().stream()).
                map(execSpec->execSpec.getSubscriberSpec().getDestination()).
                filter(DestinationSpec::isActive).
                filter(spec->!spec.getSubscribers().isEmpty()).
                anyMatch(spec -> destinationName.equals(spec.getName()));
    }


    private Response processMessagePost(QueueMessage message, DestinationSpec destinationSpec) {
        String json = jsonService.serialize(message);
        destinationSpec.message(json).send();
        return Response.accepted().entity("{\"success\":\"true\"}").build();
    }

}
