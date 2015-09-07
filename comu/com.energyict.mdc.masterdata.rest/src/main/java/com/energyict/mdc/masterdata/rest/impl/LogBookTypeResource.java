package com.energyict.mdc.masterdata.rest.impl;

import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.security.Privileges;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.rest.LogBookTypeInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
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

@Path("/logbooktypes")
public class LogBookTypeResource {

    private final MasterDataService masterDataService;
    private final DeviceConfigurationService deviceConfigurationService;

    @Inject
    public LogBookTypeResource(MasterDataService masterDataService, DeviceConfigurationService deviceConfigurationService) {
        this.masterDataService = masterDataService;
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_MASTER_DATA, Privileges.Constants.VIEW_MASTER_DATA})
    public PagedInfoList getLogbookTypes(@BeanParam JsonQueryParameters queryParameters) {
        List<LogBookTypeInfo> logbookTypeInfos = new ArrayList<>();
        // TODO it will be better to change the result type of masterDataService.findAllLogBookTypes() to Finder, as for masterDataService.findAllMeasurementTypes
        List<LogBookType> logbookTypes = this.masterDataService.findAllLogBookTypes().from(queryParameters).find();
      /*  Collections.sort(logbookTypes, new Comparator<LogBookType>() {
            @Override
            public int compare(LogBookType o1, LogBookType o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });*/
        return PagedInfoList.fromPagedList("logbookTypes", LogBookTypeInfo.from(logbookTypes), queryParameters);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_MASTER_DATA, Privileges.Constants.VIEW_MASTER_DATA})
    public PagedInfoList getLogbookType(@PathParam("id") long id, @BeanParam JsonQueryParameters queryParameters) {
        Optional<LogBookType> logBookRef = masterDataService.findLogBookType(id);
        if (!logBookRef.isPresent()) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        LogBookType logBookType = logBookRef.get();
        List<DeviceConfiguration> deviceConfigurations = this.deviceConfigurationService.findDeviceConfigurationsUsingLogBookType(logBookType);
        return PagedInfoList.fromPagedList("logbookType", Collections.singletonList(LogBookTypeInfo.from(logBookType, !deviceConfigurations.isEmpty())), queryParameters);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_MASTER_DATA)
    public Response addLogBookType(LogBookTypeInfo logbook) {
        LogBookType newLogbook = masterDataService.newLogBookType(logbook.name, logbook.obisCode);
        newLogbook.save();
        return Response.status(Response.Status.CREATED).entity(LogBookTypeInfo.from(newLogbook)).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_MASTER_DATA)
    public Response updateLogBookType(@PathParam("id") long id, LogBookTypeInfo logbook) {
        Optional<LogBookType> logBookRef = masterDataService.findLogBookType(id);
        if (!logBookRef.isPresent()) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        LogBookType editLogbook = logBookRef.get();
        editLogbook.setName(logbook.name);
        editLogbook.setObisCode(logbook.obisCode);
        editLogbook.save();
        return Response.ok(LogBookTypeInfo.from(editLogbook)).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_MASTER_DATA)
    public Response deleteLogBookType(@PathParam("id") long id) {
        Optional<LogBookType> logBookRef = masterDataService.findLogBookType(id);
        if (!logBookRef.isPresent()) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        logBookRef.get().delete();
        return Response.status(Response.Status.OK).build();
    }
}
