package com.energyict.mdc.masterdata.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.rest.LogBookTypeInfo;
import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Path("/logbooktypes")
public class LogBookResource {

    private final MasterDataService masterDataService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final Thesaurus thesaurus;

    @Inject
    public LogBookResource(MasterDataService masterDataService, DeviceConfigurationService deviceConfigurationService, Thesaurus thesaurus) {
        this.masterDataService = masterDataService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.thesaurus = thesaurus;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getLogbookTypes(@BeanParam QueryParameters queryParameters) {
        List<LogBookTypeInfo> logbookTypeInfos = new ArrayList<>();
        // TODO it will be better to change the result type of masterDataService.findAllLogBookTypes() to Finder, as for masterDataService.findAllMeasurementTypes
        List<LogBookType> logbookTypes = masterDataService.findAllLogBookTypes();
        Collections.sort(logbookTypes, new Comparator<LogBookType>() {
            @Override
            public int compare(LogBookType o1, LogBookType o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });
        return PagedInfoList.asJson("data", LogBookTypeInfo.from(logbookTypes), queryParameters);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getLogbookType(@PathParam("id") long id, @BeanParam QueryParameters queryParameters) {
        Optional<LogBookType> logBookRef = masterDataService.findLogBookType(id);
        if (!logBookRef.isPresent()) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        LogBookType logBookType = logBookRef.get();
        List<DeviceConfiguration> deviceConfigurations = this.deviceConfigurationService.findDeviceConfigurationsUsingLogBookType(logBookType);
        return PagedInfoList.asJson("data", Collections.singletonList(LogBookTypeInfo.from(logBookType, !deviceConfigurations.isEmpty())), queryParameters);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addLogBook(LogBookTypeInfo logbook) {
        LogBookType newLogbook = masterDataService.newLogBookType(logbook.name, logbook.obis);
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
        editLogbook.setObisCode(logbook.obis);
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
