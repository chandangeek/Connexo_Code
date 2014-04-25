package com.energyict.mdc.masterdata.rest.impl;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/logbooktypes")
public class MasterDataResource {
    private MasterDataService masterDataService;

    @Inject
    public MasterDataResource(MasterDataService masterDataService) {
        this.masterDataService = masterDataService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getLogbookTypes(@BeanParam QueryParameters queryParameters) {
        List<LogBookTypeInfo> logbookTypeInfos = new ArrayList<>();
        List<LogBookType> logbookTypes = masterDataService.findAllLogBookTypes();
        for (LogBookType logbookType : logbookTypes) {
            logbookTypeInfos.add(new LogBookTypeInfo(logbookType));
        }
        return PagedInfoList.asJson("data", logbookTypeInfos, queryParameters);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public RootEntity<LogBookTypeInfo> getLogbookType(@PathParam("id") long id) {
        Optional<LogBookType> logBookRef = masterDataService.findLogBookType(id);
        if (!logBookRef.isPresent()) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        return new RootEntity<LogBookTypeInfo>(new LogBookTypeInfo(logBookRef.get()));
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addLogBook(LogBookTypeInfo logbook) {
        LogBookType newLogbook = masterDataService.newLogBookType(logbook.name, ObisCode.fromString(logbook.obis));
        newLogbook.save();
        return Response.status(Response.Status.CREATED).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateLogBook(@PathParam("id") long id, LogBookTypeInfo logbook) {
        Optional<LogBookType> logBookRef = masterDataService.findLogBookType(id);
        if (!logBookRef.isPresent()) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        LogBookType editLogbook = logBookRef.get();
        editLogbook.setName(logbook.name);
        editLogbook.setObisCode(ObisCode.fromString(logbook.obis));
        editLogbook.save();
        return Response.status(Response.Status.OK).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteLogBook(@PathParam("id") long id) {
        Optional<LogBookType> logBookRef = masterDataService.findLogBookType(id);
        if (!logBookRef.isPresent()) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        logBookRef.get().delete();
        return Response.status(Response.Status.OK).build();
    }
}
