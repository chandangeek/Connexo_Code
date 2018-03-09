/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging.rest.impl;

import com.elster.jupiter.appserver.security.Privileges;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageSeeds;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.*;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Path("/destinationspec")
public class DestinationSpecResource {

    private static final String QUEUE_TABLE_NAME = "MSG_QUEUE_";

    private final MessageService messageService;
    private final TransactionService transactionService;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final DestinationSpecInfoFactory destinationSpecInfoFactory;
    private final Thesaurus thesaurus;

    @Inject
    public DestinationSpecResource(MessageService messageService, TransactionService transactionService, ConcurrentModificationExceptionFactory conflictFactory, DestinationSpecInfoFactory destinationSpecInfoFactory, Thesaurus thesaurus) {
        this.messageService = messageService;
        this.transactionService = transactionService;
        this.conflictFactory = conflictFactory;
        this.destinationSpecInfoFactory = destinationSpecInfoFactory;
        this.thesaurus = thesaurus;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_APPSEVER, Privileges.Constants.ADMINISTRATE_APPSEVER})
    public PagedInfoList getDestinationSpecs(@BeanParam JsonQueryParameters queryParameters, @QueryParam("state") boolean withState) {
        List<DestinationSpec> destinationSpecs = messageService.findDestinationSpecs();
        List<DestinationSpecInfo> destinationSpecInfos = destinationSpecs
                .stream()
                .sorted(Comparator.comparing(DestinationSpec::getName))
                .map(mapToInfo(withState))
                .skip(queryParameters.getStart().orElse(0))
                .limit(queryParameters.getLimit().map(i -> i++).orElse(Integer.MAX_VALUE))
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("destinationSpecs", destinationSpecInfos, queryParameters);
    }

    private Function<? super DestinationSpec, ? extends DestinationSpecInfo> mapToInfo(@QueryParam("state") boolean withState) {
        return withState
                ? destinationSpecInfoFactory::withAppServers
                : destinationSpecInfoFactory::from;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{destionationSpecName}")
    @RolesAllowed({Privileges.Constants.VIEW_APPSEVER, Privileges.Constants.ADMINISTRATE_APPSEVER})
    public DestinationSpecInfo getAppServer(@PathParam("destionationSpecName") String destinationSpecName, @QueryParam("state") boolean withState) {
        DestinationSpec destinationSpec = fetchDestinationSpec(destinationSpecName);
        DestinationSpecInfo destinationSpecInfo = mapToInfo(withState).apply(destinationSpec);
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
        try (TransactionContext context = transactionService.getContext()) {
            destinationSpec.purgeErrors();
            context.commit();
        }
        return Response.status(Response.Status.OK).entity(destinationSpecInfoFactory.from(destinationSpec)).build();
    }

    private Response doUpdateDestinationSpec(String destinationSpecName, DestinationSpecInfo info) {
        DestinationSpec destinationSpec = null;
        try (TransactionContext context = transactionService.getContext()) {
            destinationSpec = lockDestinationSpec(destinationSpecName, info.version);
            destinationSpec.updateRetryBehavior(info.numberOfRetries, Duration.ofSeconds(info.retryDelayInSeconds));
            context.commit();
        }
        return Response.status(Response.Status.OK).entity(destinationSpecInfoFactory.from(destinationSpec)).build();
    }

    @POST
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_APPSEVER, Privileges.Constants.ADMINISTRATE_APPSEVER})
    public Response doCreateDestinationSpec(DestinationSpecInfo info) {
        if (info.name == null || info.name.isEmpty()) {
            return buildErrorResponse4("name", MessageSeeds.EMPTY_QUEUE_NAME);
        }

        if (info.queueTypeName == null || info.queueTypeName.isEmpty()) {
            return buildErrorResponse4("queueTypeName", MessageSeeds.EMPTY_QUEUE_TYPE_NAME);
        }

        if (messageService.getDestinationSpec(info.name).isPresent()) {
            return buildErrorResponse4("name", MessageSeeds.DUPLICATE_QUEUE);
        }

        QueueTableSpec queueTableSpec = messageService.createQueueTableSpec(QUEUE_TABLE_NAME + info.name.toUpperCase(), "RAW", false);
        DestinationSpec destinationSpec = queueTableSpec.createDestinationSpec(info.name, 60, 5, false, info.queueTypeName);
        destinationSpec.activate();

        return Response.status(Response.Status.OK).build();
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
    public Response deleteDestinationSpec(@PathParam("destinationSpecName") String
                                                  destinationSpecName, DestinationSpecInfo info) {
        if (!messageService.getDestinationSpec(destinationSpecName).isPresent()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        if (messageService.getDestinationSpec(destinationSpecName).get().isDefault()) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }

        DestinationSpec destinationSpec = messageService.getDestinationSpec(destinationSpecName).get();
        QueueTableSpec queueTableSpec = destinationSpec.getQueueTableSpec();
        destinationSpec.delete();
        queueTableSpec.delete();

        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Path("/queuetypenames")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_APPSEVER, Privileges.Constants.ADMINISTRATE_APPSEVER})
    public Response getDestinationSpecTypeNames() {
        List<String> aList = Arrays.asList("DataExport", "EstimationTask", "DataValidation");

        List<String> destinationSpecTypeNames = messageService.findDestinationSpecs().stream().map(DestinationSpec::getName)
                .filter(queueName -> aList.contains(queueName)).collect(Collectors.toList());

        return Response.status(Response.Status.OK).entity(destinationSpecTypeNames).build();
    }

}
