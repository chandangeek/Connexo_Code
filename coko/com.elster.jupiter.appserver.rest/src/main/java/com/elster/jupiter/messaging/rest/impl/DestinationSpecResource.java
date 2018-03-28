/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging.rest.impl;

import com.elster.jupiter.appserver.security.Privileges;
import com.elster.jupiter.messaging.*;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.*;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
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

    @Inject
    public DestinationSpecResource(MessageService messageService, TransactionService transactionService, ConcurrentModificationExceptionFactory conflictFactory, DestinationSpecInfoFactory destinationSpecInfoFactory, Thesaurus thesaurus, TaskService taskService) {
        this.messageService = messageService;
        this.transactionService = transactionService;
        this.conflictFactory = conflictFactory;
        this.destinationSpecInfoFactory = destinationSpecInfoFactory;
        this.thesaurus = thesaurus;
        this.taskService = taskService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_APPSEVER, Privileges.Constants.ADMINISTRATE_APPSEVER})
    public PagedInfoList getDestinationSpecs(@BeanParam JsonQueryParameters queryParameters, @QueryParam("state") boolean withState) {
        List<DestinationSpec> destinationSpecs = messageService.findDestinationSpecs();
        List<RecurrentTask> allTasks = taskService.getRecurrentTasks();

        List<DestinationSpecInfo> destinationSpecInfos = destinationSpecs
                .stream()
                .sorted(Comparator.comparing(DestinationSpec::getName))
                .map((DestinationSpec spec) -> mapToInfo(withState, spec, allTasks))
                .skip(queryParameters.getStart().orElse(0))
                .limit(queryParameters.getLimit().map(i -> i++).orElse(Integer.MAX_VALUE))
                .collect(Collectors.toList());

        return PagedInfoList.fromPagedList("destinationSpecs", destinationSpecInfos, queryParameters);
    }

    private DestinationSpecInfo mapToInfo(@QueryParam("state") boolean withState, DestinationSpec destinationSpec, List<RecurrentTask> tasks) {
        return withState
                ? destinationSpecInfoFactory.withAppServers(destinationSpec, tasks)
                : destinationSpecInfoFactory.from(destinationSpec, tasks);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{destionationSpecName}")
    @RolesAllowed({Privileges.Constants.VIEW_APPSEVER, Privileges.Constants.ADMINISTRATE_APPSEVER})
    public DestinationSpecInfo getAppServer(@PathParam("destionationSpecName") String destinationSpecName, @QueryParam("state") boolean withState) {
        DestinationSpec destinationSpec = fetchDestinationSpec(destinationSpecName);
        List<RecurrentTask> allTasks = taskService.getRecurrentTasks();
        DestinationSpecInfo destinationSpecInfo = mapToInfo(withState, destinationSpec, allTasks);
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
        try (TransactionContext context = transactionService.getContext()) {
            destinationSpec.purgeErrors();
            context.commit();
        }
        return Response.status(Response.Status.OK).entity(destinationSpecInfoFactory.from(destinationSpec, allTasks)).build();
    }

    private Response doUpdateDestinationSpec(String destinationSpecName, DestinationSpecInfo info) {
        DestinationSpec destinationSpec = null;
        try (TransactionContext context = transactionService.getContext()) {
            destinationSpec = lockDestinationSpec(destinationSpecName, info.version);
            destinationSpec.updateRetryBehavior(info.numberOfRetries, Duration.ofSeconds(info.retryDelayInSeconds));
            context.commit();
        }

        List<RecurrentTask> allTasks = taskService.getRecurrentTasks();
        return Response.status(Response.Status.OK).entity(destinationSpecInfoFactory.from(destinationSpec, allTasks)).build();
    }

    @POST
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_APPSEVER, Privileges.Constants.ADMINISTRATE_APPSEVER})
    public Response doCreateDestinationSpec(DestinationSpecInfo info) {
        DestinationSpecBean bean = new DestinationSpecBean(info.name, info.queueTypeName);
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
        QueueTableSpec queueTableSpec = messageService.createQueueTableSpec(QUEUE_TABLE_NAME + bean.getName(), "RAW", false);
        DestinationSpec destinationSpec = queueTableSpec.createDestinationSpec(bean.getName(), DEFAULT_RETRY_DELAY_IN_SECONDS, DEFAULT_RETRIES, IS_DEFAULT, bean.getQueueTypeName(), ENABLE_EXTRA_QUEUE_CREATION);
        destinationSpec.activate();
        destinationSpec.subscribe(new SubscriberName(bean.getName()), defaultSubscriber.getNlsComponent(), defaultSubscriber.getNlsLayer(), defaultSubscriber.getFilterCondition());

        return Response.status(Response.Status.OK).build();
    }

    private SubscriberSpec getSubscriber4(String queueTypeName) {
        return messageService.getSubscribers().stream()
                .filter((SubscriberSpec s) -> s.getDestination().getName().equals(queueTypeName)).findFirst()
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
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }

        DestinationSpec destinationSpec = messageService.getDestinationSpec(destinationSpecName).get();
        QueueTableSpec queueTableSpec = destinationSpec.getQueueTableSpec();
        destinationSpec.unSubscribe(destinationSpecName);
        destinationSpec.delete();
        queueTableSpec.delete();

        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Path("/queuetypenames")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_APPSEVER, Privileges.Constants.ADMINISTRATE_APPSEVER})
    public Response getDestinationSpecTypeNames() {
        List<DestinationSpecTypeNameInfo> destinationSpecTypeNames = messageService.findDestinationSpecs().stream()
                .filter(DestinationSpec::isExtraQueueCreationEnabled)
                .filter(DestinationSpec::isDefault)
                .map(d -> new DestinationSpecTypeNameInfo(d.getQueueTypeName()))
                .collect(Collectors.toList());

        return Response.status(Response.Status.OK).entity(destinationSpecTypeNames).build();
    }

}
