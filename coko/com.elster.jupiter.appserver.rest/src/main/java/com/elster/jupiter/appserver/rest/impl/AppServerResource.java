package com.elster.jupiter.appserver.rest.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.appserver.SubscriberExecutionSpec;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.collections.Zipper;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.cron.CronExpressionParser;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Path("/appserver")
public class AppServerResource {

    private final RestQueryService queryService;
    private final AppService appService;
    private final MessageService messageService;
    private final TransactionService transactionService;
    private final CronExpressionParser cronExpressionParser;

    @Inject
    public AppServerResource(RestQueryService queryService, AppService appService, MessageService messageService, TransactionService transactionService, CronExpressionParser cronExpressionParser) {
        this.queryService = queryService;
        this.appService = appService;
        this.messageService = messageService;
        this.transactionService = transactionService;
        this.cronExpressionParser = cronExpressionParser;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public AppServerInfos getAppservers(@Context UriInfo uriInfo) {
        QueryParameters params = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<AppServer> appServers = queryAppServers(params);
        AppServerInfos infos = new AppServerInfos(params.clipToLimit(appServers));
        infos.total = params.determineTotal(appServers.size());
        return infos;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{appserverName}")
    public AppServerInfo getAppServer(@PathParam("appserverName") String appServerName) {
        return AppServerInfo.of(fetchAppServer(appServerName));
    }

    private AppServer fetchAppServer(String appServerName) {
        return appService.findAppServer(appServerName).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addAppServer(AppServerInfo info) {
        AppServer appServer = null;
        try(TransactionContext context = transactionService.getContext()) {
            AppServer underConstruction = appService.createAppServer(info.name, cronExpressionParser.parse("0 0 * * * ? *").get());
            if (info.executionSpecs != null) {
                info.executionSpecs.stream()
                        .forEach(spec -> {
                            SubscriberSpec subscriberSpec = messageService.getSubscriberSpec(spec.subscriberSpec.destination, spec.subscriberSpec.subsriber).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
                            underConstruction.createSubscriberExecutionSpec(subscriberSpec, spec.numberOfThreads);
                        });
            }
            if (info.active) {
                underConstruction.activate();
            } else {
                underConstruction.deactivate();
            }
            appServer = underConstruction;
            context.commit();
        }
        return Response.status(Response.Status.CREATED).entity(AppServerInfo.of(appServer)).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{appserverName}")
    public Response updateAppServer(@PathParam("appserverName") String appServerName, AppServerInfo info) {
        AppServer appServer = fetchAppServer(appServerName);
        try(TransactionContext context = transactionService.getContext()) {
            doUpdateAppServer(info, appServer);
            context.commit();
        }
        return Response.status(Response.Status.CREATED).entity(AppServerInfo.of(appServer)).build();
    }

    @DELETE
    @Path("/{appserverName}/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeDataExportTask(@PathParam("appserverName") String appServerName) {
        AppServer appServer = fetchAppServer(appServerName);
        try(TransactionContext context = transactionService.getContext()) {
            appServer.delete();
            context.commit();
        }
        return Response.status(Response.Status.OK).build();
    }

    private void doUpdateAppServer(AppServerInfo info, AppServer appServer) {
        Zipper<SubscriberExecutionSpec, SubscriberExecutionSpecInfo> zipper = new Zipper<>((s, i) -> i.matches(s));
        List<Pair<SubscriberExecutionSpec, SubscriberExecutionSpecInfo>> pairs = zipper.zip(appServer.getSubscriberExecutionSpecs(), info.executionSpecs);

        try (AppServer.BatchUpdate updater = appServer.forBatchUpdate()) {
            doThreadUpdates(pairs, updater);
            doRemovals(pairs, updater);
            doAdditions(pairs, updater);

            if (info.active) {
                updater.activate();
            } else {
                updater.deactivate();
            }
        }
    }

    private void doAdditions(List<Pair<SubscriberExecutionSpec, SubscriberExecutionSpecInfo>> pairs, AppServer.BatchUpdate updater) {
        List<Pair<SubscriberSpec, SubscriberExecutionSpecInfo>> toAdd = pairs.stream()
                .filter(pair -> pair.getFirst() == null)
                .map(pair -> pair.withFirst((f, l) -> messageService.getSubscriberSpec(l.subscriberSpec.destination, l.subscriberSpec.subsriber).orElse(null)))
                .collect(Collectors.toList());

        if (toAdd.stream().anyMatch(pair -> pair.getFirst() == null)) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        toAdd.forEach(pair -> updater.createSubscriberExecutionSpec(pair.getFirst(), pair.getLast().numberOfThreads));
    }

    private void doRemovals(List<Pair<SubscriberExecutionSpec, SubscriberExecutionSpecInfo>> pairs, AppServer.BatchUpdate updater) {
        pairs.stream()
                .filter(pair -> pair.getLast() == null)
                .map(Pair::getFirst)
                .forEach(updater::removeSubscriberExecutionSpec);
    }

    private void doThreadUpdates(List<Pair<SubscriberExecutionSpec, SubscriberExecutionSpecInfo>> pairs, AppServer.BatchUpdate updater) {
        pairs.stream()
                .filter(pair -> pair.getFirst() != null)
                .filter(pair -> pair.getLast() != null)
                .filter(pair -> pair.getFirst().getThreadCount() != pair.getLast().numberOfThreads)
                .map(pair -> pair.withLast((f, l) -> l.numberOfThreads))
                .forEach(pair -> updater.setThreadCount(pair.getFirst(), pair.getLast()));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{appserverName}/unserved")
    public SubscriberSpecInfos getSubscribers(@PathParam("appserverName") String appServerName) {
        List<SubscriberSpec> served = appService.findAppServer(appServerName)
                .map(AppServer::getSubscriberExecutionSpecs)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(SubscriberExecutionSpec::getSubscriberSpec)
                .collect(Collectors.toList());
        List<SubscriberSpec> subscribers = messageService.getSubscribers().stream()
                .filter(sub -> served.stream()
                        .filter(s -> sub.getName().equals(s.getName()))
                        .map(SubscriberSpec::getDestination)
                        .noneMatch(d -> sub.getDestination().getName().equals(d.getName()))
                )
                .collect(Collectors.toList());
        SubscriberSpecInfos subscriberSpecInfos = new SubscriberSpecInfos(subscribers);
        subscriberSpecInfos.subscriberSpecs.sort(Comparator.comparing(SubscriberSpecInfo::getDestination).thenComparing(SubscriberSpecInfo::getSubsriber));
        return subscriberSpecInfos;
    }

    private List<AppServer> queryAppServers(QueryParameters queryParameters) {
        Query<AppServer> query = appService.getAppServerQuery();
        RestQuery<AppServer> restQuery = queryService.wrap(query);
        return restQuery.select(queryParameters, Order.ascending("name"));
    }


}
