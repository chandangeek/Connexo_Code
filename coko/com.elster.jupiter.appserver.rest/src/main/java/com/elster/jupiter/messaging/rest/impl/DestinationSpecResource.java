package com.elster.jupiter.messaging.rest.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.appserver.security.Privileges;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Path("/destinationspec")
public class DestinationSpecResource {

    private final MessageService messageService;
    private final TransactionService transactionService;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final AppService appService;
    private final Thesaurus thesaurus;

    @Inject
    public DestinationSpecResource(MessageService messageService, TransactionService transactionService, ConcurrentModificationExceptionFactory conflictFactory, AppService appService, Thesaurus thesaurus) {
        this.messageService = messageService;
        this.transactionService = transactionService;
        this.conflictFactory = conflictFactory;
        this.appService = appService;
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
                ? (destinationSpec -> DestinationSpecInfo.from(destinationSpec).withStats(destinationSpec))
                : DestinationSpecInfo::from;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{destionationSpecName}")
    @RolesAllowed({Privileges.Constants.VIEW_APPSEVER, Privileges.Constants.ADMINISTRATE_APPSEVER})
    public DestinationSpecInfo getAppServer(@PathParam("destionationSpecName") String destinationSpecName, @QueryParam("state") boolean withState) {
        DestinationSpec destinationSpec = fetchDestinationSpec(destinationSpecName);
        DestinationSpecInfo destinationSpecInfo = mapToInfo(withState).apply(destinationSpec);
        if (withState) {
            destinationSpecInfo.withAppServers(destinationSpec, appService, thesaurus);
        }
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
    public Response updateAppServer(@PathParam("destionationSpecName") String destinationSpecName, DestinationSpecInfo info) {
        DestinationSpec destinationSpec = null;
        try (TransactionContext context = transactionService.getContext()) {
            destinationSpec = lockDestinationSpec(destinationSpecName, info.version);
            destinationSpec.updateRetryBehavior(info.numberOfRetries, Duration.ofSeconds(info.retryDelayInSeconds));
            context.commit();
        }
        return Response.status(Response.Status.CREATED).entity(DestinationSpecInfo.from(destinationSpec)).build();
    }

}
