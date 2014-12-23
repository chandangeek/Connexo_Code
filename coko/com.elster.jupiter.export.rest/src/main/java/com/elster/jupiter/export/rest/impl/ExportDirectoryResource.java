package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/exportdirs")
public class ExportDirectoryResource {

    private final DataExportService dataExportService;
    private final AppService appService;
    private final TransactionService transactionService;

    @Inject
    public ExportDirectoryResource(DataExportService dataExportService, AppService appService, TransactionService transactionService) {
        this.dataExportService = dataExportService;
        this.appService = appService;
        this.transactionService = transactionService;
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DirectoryForAppServerInfos getExportPaths() {

        return new DirectoryForAppServerInfos(dataExportService.getAllExportDirecties());
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{appServerName}")
    public Response getExportPaths(@PathParam("appServerName") String appServerName, DirectoryForAppServerInfo info) {
        AppServer appServer = appService.findAppServer(appServerName).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        try (TransactionContext context = transactionService.getContext()) {
            dataExportService.setExportDirectory(appServer, info.path());

            context.commit();
        }
        return Response.status(Response.Status.CREATED).entity(new DirectoryForAppServerInfo(appServer, info.path())).build();
    }

}
