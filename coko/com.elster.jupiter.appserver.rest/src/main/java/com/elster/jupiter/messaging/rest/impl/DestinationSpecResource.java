/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging.rest.impl;

import com.elster.jupiter.appserver.security.Privileges;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
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

    private static final String QUEUE_TABLE_NAME = "MSG_RAWQUEUETABLE_";

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
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_APPSEVER, Privileges.Constants.ADMINISTRATE_APPSEVER})
    public Response doCreateDestinationSpec(@FormParam("destinationSpecName") String destinationSpecName, @FormParam("destinationSpecTypeName") String destinationSpecTypeName) {
        if (destinationSpecName == null || destinationSpecName.isEmpty()) {
            throwException("error.destinationspec.name.empty", "Queue name is missing from request.", Response.Status.BAD_REQUEST);
        }

        if (destinationSpecTypeName == null || destinationSpecTypeName.isEmpty()) {
            throwException("error.destinationspec.type.empty", "Queue type is missing from request.", Response.Status.BAD_REQUEST);
        }

        if (messageService.getDestinationSpec(destinationSpecName).isPresent()) {
            throwException("error.destinationspec.duplicate", "Queue name is already used.", Response.Status.BAD_REQUEST);
        }

        createQueue(destinationSpecName, destinationSpecTypeName);

        return Response.status(Response.Status.OK).build();
    }

    private void throwException(String key, String defaultMessage, Response.Status status) {
        throw new WebApplicationException(Response.status(status).entity(thesaurus.getString(key, defaultMessage)).build());
    }

    private void createQueue(String destinationSpecName, String destinationSpecTypeName) {
        QueueTableSpec queueTableSpec = messageService.createQueueTableSpec(QUEUE_TABLE_NAME + destinationSpecName, "RAW", false);

        DestinationSpec destinationSpec = queueTableSpec.createDestinationSpec(destinationSpecName, 60, 5, false, destinationSpecTypeName);
        destinationSpec.save();
        destinationSpec.activate();
    }

    @DELETE
    @Path("/{destinationSpecName}")
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_APPSEVER, Privileges.Constants.ADMINISTRATE_APPSEVER})
    public Response deleteDestinationSpec(@PathParam("destinationSpecName") String destinationSpecName) {
        if (!messageService.getDestinationSpec(destinationSpecName).isPresent()) {
            throwException("error.destinationspec.queue.missing", "Queue is missing.", Response.Status.NOT_FOUND);
        }

        if (!messageService.getQueueTableSpec(QUEUE_TABLE_NAME + destinationSpecName).isPresent()) {
            throwException("error.destinationspec.queuetable.missing", "Queue table is missing.", Response.Status.NOT_FOUND);
        }

        if (messageService.getDestinationSpec(destinationSpecName).get().isDefault()) {
            throwException("error.destinationspec.queue.isdefault", "Default queue cannot be deleted.", Response.Status.BAD_REQUEST);
        }

        deleteQueue(destinationSpecName);

        return Response.status(Response.Status.OK).build();
    }

    private void deleteQueue(String destinationSpecName) {
        DestinationSpec destinationSpec = messageService.getDestinationSpec(destinationSpecName).get();
        QueueTableSpec queueTableSpec = destinationSpec.getQueueTableSpec();
        destinationSpec.delete();
        queueTableSpec.deactivate();
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
