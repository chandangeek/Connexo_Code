package com.elster.jupiter.appserver.rest.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.appserver.ImportScheduleOnAppServer;
import com.elster.jupiter.appserver.SubscriberExecutionSpec;
import com.elster.jupiter.appserver.security.Privileges;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.collections.Zipper;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.streams.Functions;

import javax.annotation.security.RolesAllowed;
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
import java.nio.file.FileSystem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Path("/appserver")
public class AppServerResource {

    private final RestQueryService queryService;
    private final AppService appService;
    private final MessageService messageService;
    private final FileImportService fileImportService;
    private final Thesaurus thesaurus;
    private final TransactionService transactionService;
    private final CronExpressionParser cronExpressionParser;

    private final DataExportService dataExportService;
    private final FileSystem fileSystem;

    @Inject
    public AppServerResource(RestQueryService queryService, AppService appService, MessageService messageService, FileImportService fileImportService, TransactionService transactionService, CronExpressionParser cronExpressionParser, Thesaurus thesaurus, DataExportService dataExportService, FileSystem fileSystem) {
        this.queryService = queryService;
        this.appService = appService;
        this.messageService = messageService;
        this.fileImportService = fileImportService;
        this.transactionService = transactionService;
        this.cronExpressionParser = cronExpressionParser;
        this.thesaurus = thesaurus;
        this.dataExportService = dataExportService;
        this.fileSystem = fileSystem;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_APPSEVER, Privileges.ADMINISTRATE_APPSEVER})
    public AppServerInfos getAppservers(@Context UriInfo uriInfo) {
        QueryParameters params = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<AppServer> appServers = queryAppServers(params);
        AppServerInfos infos = new AppServerInfos();

        infos.appServers = appServers.stream()
                .map(appServer -> {
                    String exportDir = dataExportService.getExportDirectory(appServer).map(Object::toString).orElse(null);
                    String importDir = appServer.getImportDirectory().map(Object::toString).orElse(null);
                    return AppServerInfo.of(appServer, importDir, exportDir, thesaurus);
                })
                .collect(Collectors.toCollection(ArrayList::new));

        infos.total = params.determineTotal(appServers.size());
        return infos;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{appserverName}")
    @RolesAllowed({Privileges.VIEW_APPSEVER, Privileges.ADMINISTRATE_APPSEVER})
    public AppServerInfo getAppServer(@PathParam("appserverName") String appServerName) {
        AppServer appServer = fetchAppServer(appServerName);
        String importPath = appServer.getImportDirectory().map(Object::toString).orElse(null);
        String exportPath = dataExportService.getExportDirectory(appServer).map(Object::toString).orElse(null);
        return AppServerInfo.of(appServer, importPath, exportPath, thesaurus);
    }

    private AppServer fetchAppServer(String appServerName) {
        return appService.findAppServer(appServerName).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.ADMINISTRATE_APPSEVER})
    public Response addAppServer(AppServerInfo info) {
        AppServer appServer = null;
        try (TransactionContext context = transactionService.getContext()) {
            AppServer underConstruction = appService.createAppServer(info.name, cronExpressionParser.parse("0 0 * * * ? *").get());
            try (AppServer.BatchUpdate batchUpdate = underConstruction.forBatchUpdate()) {
                if (info.executionSpecs != null) {
                    info.executionSpecs.stream()
                            .forEach(spec -> {
                                SubscriberSpec subscriberSpec = messageService.getSubscriberSpec(spec.subscriberSpec.destination, spec.subscriberSpec.subscriber).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
                                if (spec.active) {
                                    batchUpdate.createActiveSubscriberExecutionSpec(subscriberSpec, spec.numberOfThreads);
                                } else {
                                    batchUpdate.createInactiveSubscriberExecutionSpec(subscriberSpec, spec.numberOfThreads);
                                }
                            });
                }
                if (info.importServices != null) {
                    info.importServices.stream()
                            .map(ImportScheduleInfo::getId)
                            .map(fileImportService::getImportSchedule)
                            .flatMap(Functions.asStream())
                            .forEach(batchUpdate::addImportScheduleOnAppServer);
                }
                if (info.active) {
                    batchUpdate.activate();
                } else {
                    batchUpdate.deactivate();
                }
            }
            appServer = underConstruction;
            if (info.exportDirectory != null) {
                dataExportService.setExportDirectory(appServer, fileSystem.getPath(info.exportDirectory));
            }
            if (info.importDirectory != null) {
                appServer.setImportDirectory(fileSystem.getPath(info.importDirectory));
            }
            context.commit();
        }
        return Response.status(Response.Status.CREATED).entity(AppServerInfo.of(appServer, info.importDirectory, info.exportDirectory, thesaurus)).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{appserverName}")
    @RolesAllowed({Privileges.ADMINISTRATE_APPSEVER})
    public Response updateAppServer(@PathParam("appserverName") String appServerName, AppServerInfo info) {
        AppServer appServer = fetchAppServer(appServerName);
        try (TransactionContext context = transactionService.getContext()) {
            doUpdateAppServer(info, appServer);

            String currentExportDir = dataExportService.getExportDirectory(appServer)
                    .map(Object::toString)
                    .orElse(null);
            if (!Objects.equals(currentExportDir, info.exportDirectory)) {
                dataExportService.setExportDirectory(appServer, fileSystem.getPath(info.exportDirectory));
            }
            String currentImportDir = appServer.getImportDirectory()
                    .map(Object::toString)
                    .orElse(null);
            if (!Objects.equals(currentImportDir, info.importDirectory)) {
                appServer.setImportDirectory(fileSystem.getPath(info.importDirectory));
            }

            context.commit();
        }
        return Response.status(Response.Status.CREATED).entity(AppServerInfo.of(appServer, info.importDirectory, info.exportDirectory, thesaurus)).build();
    }

    @DELETE
    @Path("/{appserverName}/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_APPSEVER})
    public Response removeAppServer(@PathParam("appserverName") String appServerName) {
        AppServer appServer = fetchAppServer(appServerName);
        try (TransactionContext context = transactionService.getContext()) {
            dataExportService.removeExportDirectory(appServer);
            appServer.delete();
            context.commit();
        }
        return Response.status(Response.Status.OK).build();
    }

    @PUT
    @Path("/{appserverName}/activate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_APPSEVER})
    public Response activateAppServer(@PathParam("appserverName") String appServerName) {
        AppServer appServer = fetchAppServer(appServerName);
        if (!appServer.isActive()) {
            try (TransactionContext context = transactionService.getContext()) {
                appServer.activate();
                context.commit();
            }
        }
        return Response.status(Response.Status.OK).build();
    }

    @PUT
    @Path("/{appserverName}/deactivate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_APPSEVER})
    public Response deactivateAppServer(@PathParam("appserverName") String appServerName) {
        AppServer appServer = fetchAppServer(appServerName);
        if (appServer.isActive()) {
            try (TransactionContext context = transactionService.getContext()) {
                appServer.deactivate();
                context.commit();
            }
        }
        return Response.status(Response.Status.OK).build();
    }

    private void doUpdateAppServer(AppServerInfo info, AppServer appServer) {
        Zipper<SubscriberExecutionSpec, SubscriberExecutionSpecInfo> zipperMessageServices = new Zipper<>((s, i) -> i.matches(s));
        List<Pair<SubscriberExecutionSpec, SubscriberExecutionSpecInfo>> pairsMessageServices = zipperMessageServices.zip(appServer.getSubscriberExecutionSpecs(), info.executionSpecs);

        Zipper<ImportScheduleOnAppServer, ImportScheduleInfo> zipperImportServices = new Zipper<>((s, i) -> s.getImportSchedule().isPresent() && (i.id == s.getImportSchedule().get().getId()));
        List<Pair<ImportScheduleOnAppServer, ImportScheduleInfo>> pairsImportServices = zipperImportServices.zip(appServer.getImportSchedulesOnAppServer().stream().collect(Collectors.toList()), info.importServices);

        try (AppServer.BatchUpdate updater = appServer.forBatchUpdate()) {
            doSubscriberExecutionSpecUpdates(pairsMessageServices, updater);
            doMessageServicesRemovals(pairsMessageServices, updater);
            doMessageServicesAdditions(pairsMessageServices, updater);

            doImportServicesRemovals(pairsImportServices, updater);
            doImportServicesAdditions(pairsImportServices, updater);

            if (info.active) {
                updater.activate();
            } else {
                updater.deactivate();
            }
        }
    }

    private void doMessageServicesAdditions(List<Pair<SubscriberExecutionSpec, SubscriberExecutionSpecInfo>> pairs, AppServer.BatchUpdate updater) {
        List<Pair<SubscriberSpec, SubscriberExecutionSpecInfo>> toAdd = pairs.stream()
                .filter(pair -> pair.getFirst() == null)
                .map(pair -> pair.withFirst((f, l) -> messageService.getSubscriberSpec(l.subscriberSpec.destination, l.subscriberSpec.subscriber).orElse(null)))
                .collect(Collectors.toList());

        if (toAdd.stream().anyMatch(pair -> pair.getFirst() == null)) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        toAdd.forEach(pair -> updater.createActiveSubscriberExecutionSpec(pair.getFirst(), pair.getLast().numberOfThreads));
    }

    private void doMessageServicesRemovals(List<Pair<SubscriberExecutionSpec, SubscriberExecutionSpecInfo>> pairs, AppServer.BatchUpdate updater) {
        pairs.stream()
                .filter(pair -> pair.getLast() == null)
                .map(Pair::getFirst)
                .forEach(updater::removeSubscriberExecutionSpec);
    }

    private void doImportServicesAdditions(List<Pair<ImportScheduleOnAppServer, ImportScheduleInfo>> pairs, AppServer.BatchUpdate updater) {
        List<Pair<ImportSchedule, ImportScheduleInfo>> toAdd = pairs.stream()
                .filter(pair -> pair.getFirst() == null)
                .map(pair -> pair.withFirst((f, l) -> fileImportService.getImportSchedule(l.id).orElse(null)))
                .collect(Collectors.toList());

        if (toAdd.stream().anyMatch(pair -> pair.getFirst() == null)) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        toAdd.forEach(pair -> updater.addImportScheduleOnAppServer(pair.getFirst()));
    }

    private void doImportServicesRemovals(List<Pair<ImportScheduleOnAppServer, ImportScheduleInfo>> pairs, AppServer.BatchUpdate updater) {
        pairs.stream()
                .filter(pair -> pair.getLast() == null)
                .map(Pair::getFirst)
                .forEach(updater::removeImportScheduleOnAppServer);
    }

    private void doSubscriberExecutionSpecUpdates(List<Pair<SubscriberExecutionSpec, SubscriberExecutionSpecInfo>> pairs, AppServer.BatchUpdate updater) {
        pairs.stream()
                .filter(Pair::hasFirst)
                .filter(Pair::hasLast)
                .filter(pair -> pair.getFirst().getThreadCount() != pair.getLast().numberOfThreads)
                .map(pair -> pair.withLast((f, l) -> l.numberOfThreads))
                .forEach(pair -> updater.setThreadCount(pair.getFirst(), pair.getLast()));
        pairs.stream()
                .filter(Pair::hasFirst)
                .filter(Pair::hasLast)
                .filter(pair -> pair.getFirst().isActive() != pair.getLast().active)
                .map(pair -> pair.withLast((f, l) -> activatorDeactivator(updater, l.active)))
                .forEach(pair -> pair.getLast().accept(pair.getFirst()));
    }

    private Consumer<SubscriberExecutionSpec> activatorDeactivator(AppServer.BatchUpdate updater, boolean active) {
        return active ? updater::activate : updater::deactivate;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{appserverName}/unserved")
    @RolesAllowed({Privileges.VIEW_APPSEVER, Privileges.ADMINISTRATE_APPSEVER})
    public SubscriberSpecInfos getSubscribers(@PathParam("appserverName") String appServerName) {
        List<SubscriberSpec> served = appService.findAppServer(appServerName)
                .map(AppServer::getSubscriberExecutionSpecs)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(SubscriberExecutionSpec::getSubscriberSpec)
                .collect(Collectors.toList());
        List<SubscriberSpec> subscribers = messageService.getNonSystemManagedSubscribers().stream()
                .filter(sub -> served.stream()
                                .filter(s -> sub.getName().equals(s.getName()))
                                .map(SubscriberSpec::getDestination)
                                .noneMatch(d -> sub.getDestination().getName().equals(d.getName()))
                )
                .collect(Collectors.toList());
        SubscriberSpecInfos subscriberSpecInfos = new SubscriberSpecInfos(subscribers, thesaurus);

        /*for (SubscriberSpecInfo info : subscriberSpecInfos.subscriberSpecs) {
            info.displayName = thesaurus.getStringBeyondComponent(info.subscriber, info.subscriber);
        }*/

        subscriberSpecInfos.subscriberSpecs.sort(Comparator.comparing(SubscriberSpecInfo::getDestination).thenComparing(SubscriberSpecInfo::getSubscriber));
        return subscriberSpecInfos;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{appserverName}/servedimport")
    @RolesAllowed({Privileges.VIEW_APPSEVER, Privileges.ADMINISTRATE_APPSEVER})
    public ImportScheduleInfos getServedImportSchedules(@PathParam("appserverName") String appServerName) {
        List<ImportSchedule> served = getImportSchedulesOnAppServer(appServerName);
        for (Iterator<ImportSchedule> iterator = served.listIterator(); iterator.hasNext(); ) {
            ImportSchedule importSchedule = iterator.next();
            if (importSchedule.getObsoleteTime() != null) {
                iterator.remove();
            }
        }
        ImportScheduleInfos importScheduleInfos = new ImportScheduleInfos(served);
        importScheduleInfos.importServices.sort(Comparator.comparing(ImportScheduleInfo::getName));
        return importScheduleInfos;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{appserverName}/unservedimport")
    @RolesAllowed({Privileges.VIEW_APPSEVER, Privileges.ADMINISTRATE_APPSEVER})
    public ImportScheduleInfos getUnservedImportSchedules(@PathParam("appserverName") String appServerName) {
        List<ImportSchedule> served = getImportSchedulesOnAppServer(appServerName);
        for (Iterator<ImportSchedule> iterator = served.listIterator(); iterator.hasNext(); ) {
            ImportSchedule importSchedule = iterator.next();
            if (importSchedule.getObsoleteTime() != null) {
                iterator.remove();
            }
        }
        List<ImportSchedule> unserved = fileImportService.getImportSchedules().stream()
                .filter(schedule -> served.stream()
                                .noneMatch(s -> schedule.getName().equals(s.getName()))
                )
                .collect(Collectors.toList());

        ImportScheduleInfos importScheduleInfos = new ImportScheduleInfos(unserved);

        importScheduleInfos.importServices.sort(Comparator.comparing(ImportScheduleInfo::getName));
        return importScheduleInfos;
    }

    private List<AppServer> queryAppServers(QueryParameters queryParameters) {
        Query<AppServer> query = appService.getAppServerQuery();
        RestQuery<AppServer> restQuery = queryService.wrap(query);
        return restQuery.select(queryParameters, Order.ascending("name").toUpperCase());
    }

    private List<ImportSchedule> getImportSchedulesOnAppServer(@PathParam("appserverName") String appServerName) {
        return appService.findAppServer(appServerName)
                .map(AppServer::getImportSchedulesOnAppServer)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(ImportScheduleOnAppServer::getImportSchedule)
                .flatMap(Functions.asStream())
                .collect(Collectors.toList());
    }


}
