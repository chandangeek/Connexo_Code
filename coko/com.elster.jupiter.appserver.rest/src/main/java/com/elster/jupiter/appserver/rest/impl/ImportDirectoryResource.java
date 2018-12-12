/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.appserver.rest.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.appserver.security.Privileges;
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
import java.nio.file.FileSystem;

@Path("/importdirs")
public class ImportDirectoryResource {

    private final AppService appService;
    private final TransactionService transactionService;
    private final FileSystem fileSystem;

    @Inject
    public ImportDirectoryResource(AppService appService, TransactionService transactionService, FileSystem fileSystem) {
        this.appService = appService;
        this.transactionService = transactionService;
        this.fileSystem = fileSystem;
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_APPSEVER, Privileges.Constants.ADMINISTRATE_APPSEVER})
    public DirectoryForAppServerInfos getImportPaths() {
        return new DirectoryForAppServerInfos(appService.getAllImportDirectories());
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Path("/{appServerName}")
    @RolesAllowed({Privileges.Constants.VIEW_APPSEVER, Privileges.Constants.ADMINISTRATE_APPSEVER})
    public DirectoryForAppServerInfo getImportPathForAppServer(@PathParam("appServerName") String appServerName) {
        AppServer appServer = findAppServerOrThrowException(appServerName);
        DirectoryForAppServerInfo info = new DirectoryForAppServerInfo();
        info.appServerName = appServerName;
        appServer.getImportDirectory().ifPresent(path -> info.directory = path.toString());
        return info;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_APPSEVER})
    public Response addImportPaths(DirectoryForAppServerInfo info) {
        return updateImportPaths(info.appServerName, info);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Path("/{appServerName}")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_APPSEVER})
    public Response updateImportPaths(@PathParam("appServerName") String appServerName, DirectoryForAppServerInfo info) {
        AppServer appServer = findAppServerOrThrowException(appServerName);
        try (TransactionContext context = transactionService.getContext()) {
            appServer.setImportDirectory(fileSystem.getPath(info.directory));
            context.commit();
        }
        return Response.status(Response.Status.CREATED).entity(new DirectoryForAppServerInfo(appServer,fileSystem.getPath(info.directory))).build();
    }


    private AppServer findAppServerOrThrowException(String appServerName) {
        return appService.findAppServer(appServerName).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Path("/{appServerName}")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_APPSEVER})
    public Response removeImportPaths(@PathParam("appServerName") String appServerName){
        AppServer appServer = findAppServerOrThrowException(appServerName);
        try (TransactionContext context = transactionService.getContext()) {
            appServer.removeImportDirectory();
            context.commit();
        }
        return Response.ok().build();
    }

}
