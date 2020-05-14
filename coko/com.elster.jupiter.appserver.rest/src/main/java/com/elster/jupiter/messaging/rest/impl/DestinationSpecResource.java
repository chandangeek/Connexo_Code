/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging.rest.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.appserver.SubscriberExecutionSpec;
import com.elster.jupiter.appserver.security.Privileges;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageSeeds;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueStatus;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Path("/destinationspec")
public class DestinationSpecResource {

    private static final String QUEUE_TABLE_NAME = "MSG_QUEUE_";
    private static final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;
    private static final int DEFAULT_RETRIES = 5;
    private static final boolean ENABLE_EXTRA_QUEUE_CREATION = true;
    private static final boolean IS_DEFAULT = false;

    private final MessageService messageService;
    private final TransactionService transactionService;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final DestinationSpecInfoFactory destinationSpecInfoFactory;
    private final Thesaurus thesaurus;
    private final TaskService taskService;
    private final AppService appService;
    private final ServiceCallService serviceCallService;

    @Inject
    public DestinationSpecResource(MessageService messageService, TransactionService transactionService, ConcurrentModificationExceptionFactory conflictFactory, DestinationSpecInfoFactory destinationSpecInfoFactory, Thesaurus thesaurus,
                                   TaskService taskService, AppService appService, ServiceCallService serviceCallService) {
        this.messageService = messageService;
        this.transactionService = transactionService;
        this.conflictFactory = conflictFactory;
        this.destinationSpecInfoFactory = destinationSpecInfoFactory;
        this.thesaurus = thesaurus;
        this.taskService = taskService;
        this.appService = appService;
        this.serviceCallService = serviceCallService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_APPSEVER, Privileges.Constants.ADMINISTRATE_APPSEVER})
    public PagedInfoList getDestinationSpecs(@BeanParam JsonQueryParameters queryParameters, @QueryParam("state") boolean withState) {
        List<DestinationSpec> destinationSpecs = messageService.findDestinationSpecs();
        List<RecurrentTask> allTasks = taskService.getRecurrentTasks();
        Map<String, List<ServiceCallType>> serviceCallTypes = getServiceCallTypes();
        Map<String, QueueStatus> queuesStatuses = Optional.ofNullable(destinationSpecs).filter(ds -> withState)
                .map(Collection::stream).flatMap(s -> s.findFirst()).map(DestinationSpec::getAllQueuesStatus)
                .map(Collection::stream).map(s->s.collect(Collectors.toMap(QueueStatus::getQueueName, Function.identity())))
                .orElseGet(HashMap::new);

        List<DestinationSpecInfo> destinationSpecInfos = destinationSpecs
                .stream()
                .sorted(Comparator.comparing(DestinationSpec::getName))
                .map((DestinationSpec spec) -> mapToInfo(withState, spec, allTasks, serviceCallTypes.getOrDefault(spec.getName(), Collections.<ServiceCallType>emptyList()), queuesStatuses))
                .skip(queryParameters.getStart().orElse(0))
                .limit(queryParameters.getLimit().map(i -> i++).orElse(Integer.MAX_VALUE))
                .collect(Collectors.toList());

        return PagedInfoList.fromPagedList("destinationSpecs", destinationSpecInfos, queryParameters);
    }

    private DestinationSpecInfo mapToInfo(@QueryParam("state") boolean withState, DestinationSpec destinationSpec, List<RecurrentTask> tasks, List<ServiceCallType> serviceCallTypes,
                                          Map<String, QueueStatus> queuesStatuses) {
        return withState
                ? destinationSpecInfoFactory.withAppServers(destinationSpec, tasks, serviceCallTypes, queuesStatuses)
                : destinationSpecInfoFactory.from(destinationSpec, tasks, serviceCallTypes);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{destionationSpecName}")
    @RolesAllowed({Privileges.Constants.VIEW_APPSEVER, Privileges.Constants.ADMINISTRATE_APPSEVER})
    public DestinationSpecInfo getAppServer(@PathParam("destionationSpecName") String destinationSpecName, @QueryParam("state") boolean withState) {
        DestinationSpec destinationSpec = fetchDestinationSpec(destinationSpecName);
        List<RecurrentTask> allTasks = taskService.getRecurrentTasks();
        List<ServiceCallType> serviceCallTypes = getServiceCallTypes(destinationSpec);
        DestinationSpecInfo destinationSpecInfo = mapToInfo(withState, destinationSpec, allTasks, serviceCallTypes, null);
        return destinationSpecInfo;
    }

    private DestinationSpec fetchDestinationSpec(String destinationSpecName) {
        return messageService.getDestinationSpec(destinationSpecName).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    private DestinationSpec lockDestinationSpec(String destinationSpecName, long version) {
        return messageService.lockDestinationSpec(destinationSpecName, version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(destinationSpecName)
                        .withActualVersion(() -> messageService.getDestinationSpec(destinationSpecName)
                                .map(DestinationSpec::getVersion).orElse(null))
                        .supplier());
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{destinationSpecName}")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_APPSEVER})
    public Response updateDestinationSpec(@PathParam("destinationSpecName") String destinationSpecName, DestinationSpecInfo info, @QueryParam("purgeErrors") boolean purgeErrors) {
        if (purgeErrors) {
            return doPurgeErrors(destinationSpecName);
        }
        return doUpdateDestinationSpec(destinationSpecName, info);
    }

    private Response doPurgeErrors(String destinationSpecName) {
        DestinationSpec destinationSpec = fetchDestinationSpec(destinationSpecName);
        List<RecurrentTask> allTasks = taskService.getRecurrentTasks();
        List<ServiceCallType> serviceCallTypes = getServiceCallTypes(destinationSpec);
        try (TransactionContext context = transactionService.getContext()) {
            destinationSpec.purgeErrors();
            context.commit();
        }
        return Response.status(Response.Status.OK).entity(destinationSpecInfoFactory.from(destinationSpec, allTasks, serviceCallTypes)).build();
    }

    private Response doUpdateDestinationSpec(String destinationSpecName, DestinationSpecInfo info) {
        DestinationSpec destinationSpec = null;
        try (TransactionContext context = transactionService.getContext()) {
            destinationSpec = lockDestinationSpec(destinationSpecName, info.version);
            destinationSpec.updateRetryBehavior(info.numberOfRetries, Duration.ofSeconds(info.retryDelayInSeconds));
            context.commit();
        }

        List<RecurrentTask> allTasks = taskService.getRecurrentTasks();
        List<ServiceCallType> serviceCallTypes = getServiceCallTypes(destinationSpec);
        return Response.status(Response.Status.OK).entity(destinationSpecInfoFactory.from(destinationSpec, allTasks, serviceCallTypes)).build();
    }

    @POST
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_APPSEVER, Privileges.Constants.ADMINISTRATE_APPSEVER})
    public Response doCreateDestinationSpec(DestinationSpecInfo info) {
        DestinationSpecBean bean = new DestinationSpecBean(info.name, info.queueTypeName);

        if (bean.isReserved()) {
            return buildErrorResponse4("name", MessageSeeds.RESERVED_QUEUE_NAME);
        }

        if (bean.isWrongNameDefined()) {
            return buildErrorResponse4("name", MessageSeeds.EMPTY_QUEUE_NAME);
        }

        if (bean.isWrongQueueTypeNameDefined()) {
            return buildErrorResponse4("queueTypeName", MessageSeeds.EMPTY_QUEUE_TYPE_NAME);
        }

        if (messageService.getDestinationSpec(bean.getName()).isPresent()) {
            return buildErrorResponse4("name", MessageSeeds.DUPLICATE_QUEUE);
        }

        SubscriberSpec defaultSubscriber = getSubscriber4(bean.getQueueTypeName());
        boolean prioritized = isPrioritized(bean.getQueueTypeName());
        QueueTableSpec queueTableSpec = messageService.createQueueTableSpec(QUEUE_TABLE_NAME + bean.getName(), "RAW", null, false, prioritized);
        DestinationSpec destinationSpec = queueTableSpec.createDestinationSpec(bean.getName(), DEFAULT_RETRY_DELAY_IN_SECONDS, DEFAULT_RETRIES, IS_DEFAULT, bean.getQueueTypeName(), ENABLE_EXTRA_QUEUE_CREATION, prioritized);
        destinationSpec.activate();
        destinationSpec.subscribe(SubscriberName.from(bean.getName()), defaultSubscriber.getNlsComponent(), defaultSubscriber.getNlsLayer(), defaultSubscriber.getFilterCondition());

        return Response.status(Response.Status.OK).build();
    }

    private boolean isPrioritized(String queueTypeName) {
        return messageService.getDestinationSpecs(queueTypeName).stream().anyMatch(destinationSpec -> destinationSpec.isPrioritized());
    }

    private SubscriberSpec getSubscriber4(String queueTypeName) {
        return messageService.getSubscribers().stream()
                .filter((SubscriberSpec subscriberSpec) -> subscriberSpec.getDestination().getName().equals(queueTypeName))
                .findFirst()
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    private Response buildErrorResponse4(String field, MessageSeeds message) {
        LocalizedFieldValidationException fieldValidationException = new LocalizedFieldValidationException(message, field);
        ConstraintViolationInfo constraintViolationInfo = new ConstraintViolationInfo(thesaurus).from(fieldValidationException);
        return Response.status(Response.Status.BAD_REQUEST).entity(constraintViolationInfo).build();
    }

    @DELETE
    @Path("/{destinationSpecName}")
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_APPSEVER, Privileges.Constants.ADMINISTRATE_APPSEVER})
    public Response deleteDestinationSpec(@PathParam("destinationSpecName") String destinationSpecName, DestinationSpecInfo info) {
        if (!messageService.getDestinationSpec(destinationSpecName).isPresent()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        if (messageService.getDestinationSpec(destinationSpecName).get().isDefault()) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }

        if (!info.tasks.isEmpty()) {
            throw new WebApplicationException(thesaurus.getString(MessageSeeds.Keys.TASKS_NOT_EMPTY, MessageSeeds.TASKS_NOT_EMPTY.getDefaultFormat()),
                    Response.Status.FORBIDDEN);
        }

        if (getUsedDestinationNames().contains(destinationSpecName)) {
            throw new WebApplicationException(thesaurus.getSimpleFormat(MessageSeeds.SERVICE_CALL_TYPES_NOT_EMPTY).format(),
                    Response.Status.FORBIDDEN);
        }

        if (getSubscriberExecutionSpec4(destinationSpecName).isPresent()) {
            throw new WebApplicationException(thesaurus.getString(MessageSeeds.Keys.ACTIVE_SUBSCRIBER_DEFINED_FOR_QUEUE, MessageSeeds.ACTIVE_SUBSCRIBER_DEFINED_FOR_QUEUE.getDefaultFormat()),
                    Response.Status.FORBIDDEN);
        }

        DestinationSpec destinationSpec = messageService.getDestinationSpec(destinationSpecName).get();
        QueueTableSpec queueTableSpec = destinationSpec.getQueueTableSpec();
        destinationSpec.unSubscribe(destinationSpecName);
        destinationSpec.delete();
        queueTableSpec.delete();

        return Response.status(Response.Status.OK).build();
    }

    private List<ServiceCallType> getServiceCallTypes(DestinationSpec destinationSpec) {
        return serviceCallService.getServiceCallTypes(destinationSpec.getName());
    }

    private Map<String,List<ServiceCallType>> getServiceCallTypes() {
        return serviceCallService.getServiceCallTypes().stream()
                .collect(Collectors.groupingBy(ServiceCallType::getDestinationName));
    }

    private Set<String> getUsedDestinationNames() {
        return serviceCallService.getServiceCallTypes().find().stream().map(sc -> sc.getDestinationName()).distinct()
                .collect(Collectors.toSet());
    }

    private Optional<SubscriberExecutionSpec> getSubscriberExecutionSpec4(String destinationSpecName) {
        return appService.getSubscriberExecutionSpecs().stream()
                .filter(subscriberExecutionSpec -> subscriberExecutionSpec.getSubscriberSpec().getName().equals(destinationSpecName))
                .filter(SubscriberExecutionSpec::isActive).findAny();
    }

    @GET
    @Path("/queuetypenames")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_APPSEVER, Privileges.Constants.ADMINISTRATE_APPSEVER})
    public Response getDestinationSpecTypeNames() {
        return Response.status(Response.Status.OK).entity(DestinationSpecTypeName.from(messageService.findDestinationSpecs())).build();
    }

}

