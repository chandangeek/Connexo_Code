package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PROPFIND;
import com.energyict.mdc.multisense.api.impl.utils.FieldSelection;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.multisense.api.impl.utils.PagedInfoList;
import com.energyict.mdc.multisense.api.security.Privileges;
import com.energyict.mdc.scheduling.SchedulingService;

import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import static java.util.stream.Collectors.toList;

@Path("/comschedules")
public class ComScheduleResource {

    private final ComScheduleInfoFactory comScheduleInfoFactory;
    private final SchedulingService comScheduleService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public ComScheduleResource(SchedulingService comScheduleService, ComScheduleInfoFactory comScheduleInfoFactory, ExceptionFactory exceptionFactory) {
        this.comScheduleService = comScheduleService;
        this.comScheduleInfoFactory = comScheduleInfoFactory;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Path("/{comScheduleId}")
    @RolesAllowed(Privileges.PUBLIC_REST_API)
    public ComScheduleInfo getComSchedule(@PathParam("comScheduleId") long comScheduleId, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
         return comScheduleService.findSchedule(comScheduleId)
                 .map(ct -> comScheduleInfoFactory.from(ct, uriInfo, fieldSelection.getFields()))
                 .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_COM_SCHEDULE));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed(Privileges.PUBLIC_REST_API)
    public PagedInfoList<ComScheduleInfo> getComSchedules(@BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters) {
        List<ComScheduleInfo> infos = comScheduleService.findAllSchedules().from(queryParameters).stream()
                .map(ct -> comScheduleInfoFactory.from(ct, uriInfo, fieldSelection.getFields()))
                .collect(toList());
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(ComScheduleResource.class);
        return PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo);
    }

    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed(Privileges.PUBLIC_REST_API)
    public List<String> getFields() {
        return comScheduleInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }

}
