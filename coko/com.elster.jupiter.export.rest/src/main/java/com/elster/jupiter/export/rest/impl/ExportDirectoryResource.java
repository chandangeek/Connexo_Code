/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.security.Privileges;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.function.Supplier;

@Path("/exportdirs")
public class ExportDirectoryResource {

    private final DataExportService dataExportService;
    private final AppService appService;
    private final TransactionService transactionService;
    private final ConcurrentModificationExceptionFactory conflictFactory;

    @Inject
    public ExportDirectoryResource(DataExportService dataExportService, AppService appService, TransactionService transactionService, ConcurrentModificationExceptionFactory conflictFactory) {
        this.dataExportService = dataExportService;
        this.appService = appService;
        this.transactionService = transactionService;
        this.conflictFactory = conflictFactory;
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DATA_EXPORT_TASK, Privileges.Constants.ADMINISTRATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_SCHEDULE_DATA_EXPORT_TASK, Privileges.Constants.RUN_DATA_EXPORT_TASK})
    public DirectoryForAppServerInfos getExportPaths() {
        return new DirectoryForAppServerInfos(dataExportService.getAllExportDirecties());
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Path("/{appServerName}")
    @RolesAllowed({Privileges.Constants.VIEW_DATA_EXPORT_TASK, Privileges.Constants.ADMINISTRATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_SCHEDULE_DATA_EXPORT_TASK, Privileges.Constants.RUN_DATA_EXPORT_TASK})
    public DirectoryForAppServerInfo getExportPathForAppServer(@PathParam("appServerName") String appServerName) {
        AppServer appServer = findAppServerOrThrowException(appServerName);
        DirectoryForAppServerInfo info = new DirectoryForAppServerInfo();
        info.appServerName = appServerName;
        info.version = appServer.getVersion();
        dataExportService.getExportDirectory(appServer).ifPresent(path -> info.directory = path.toString());
        return info;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DATA_EXPORT_TASK, Privileges.Constants.ADMINISTRATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_SCHEDULE_DATA_EXPORT_TASK, Privileges.Constants.RUN_DATA_EXPORT_TASK})
    public Response addExportPaths(DirectoryForAppServerInfo info) {
        Supplier<AppServer> appServerProvider = () -> findAppServerOrThrowException(info.appServerName);
        return doExportPathUpdate(info, appServerProvider);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Path("/{appServerName}")
    @RolesAllowed({Privileges.Constants.VIEW_DATA_EXPORT_TASK, Privileges.Constants.ADMINISTRATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_SCHEDULE_DATA_EXPORT_TASK, Privileges.Constants.RUN_DATA_EXPORT_TASK})
    public Response updateExportPaths(@PathParam("appServerName") String appServerName, DirectoryForAppServerInfo info) {
        info.appServerName = appServerName;
        Supplier<AppServer> appServerProvider = () -> findAndLockAppServer(info);
        return doExportPathUpdate(info, appServerProvider);
    }

    private Response doExportPathUpdate(DirectoryForAppServerInfo info, Supplier<AppServer> appServerProvider) {
        try (TransactionContext context = transactionService.getContext()) {
            AppServer appServer = appServerProvider.get();
            dataExportService.setExportDirectory(appServer, info.path());
            context.commit();
            return Response.status(Response.Status.CREATED).entity(new DirectoryForAppServerInfo(appServer, info.path())).build();
        }
    }

    private AppServer findAppServerOrThrowException(String appServerName) {
        return appService.findAppServer(appServerName).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    private AppServer findAndLockAppServer(DirectoryForAppServerInfo info) {
        return appService.findAndLockAppServerByNameAndVersion(info.appServerName, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.appServerName)
                        .withActualVersion(() -> appService.findAppServer(info.appServerName).map(AppServer::getVersion).orElse(null))
                        .supplier());
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Path("/{appServerName}")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DATA_EXPORT_TASK)
    public Response removeExportPaths(@PathParam("appServerName") String appServerName, DirectoryForAppServerInfo info) {
        info.appServerName = appServerName;
        try (TransactionContext context = transactionService.getContext()) {
            AppServer appServer = findAndLockAppServer(info);
            dataExportService.removeExportDirectory(appServer);
            context.commit();
            return Response.ok().build();
        }
    }

}
