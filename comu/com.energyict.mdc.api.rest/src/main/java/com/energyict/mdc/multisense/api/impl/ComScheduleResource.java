package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.FieldSelection;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PagedInfoList;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PROPFIND;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.multisense.api.security.Privileges;
import com.energyict.mdc.scheduling.SchedulingService;

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
import java.util.List;

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

    /**
     * A communication schedule defines time and recurrence for reading a particular set of meter data from a device or group of devices.
     *
     * @Summary Fetch a communication schedule
     *
     * @param comScheduleId Id of the communication schedule
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @return a uniquely identified communication schedule.
     */
    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Path("/{comScheduleId}")
    @RolesAllowed(Privileges.Constants.PUBLIC_REST_API)
    public ComScheduleInfo getComSchedule(@PathParam("comScheduleId") long comScheduleId, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
         return comScheduleService.findSchedule(comScheduleId)
                 .map(ct -> comScheduleInfoFactory.from(ct, uriInfo, fieldSelection.getFields()))
                 .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_COM_SCHEDULE));
    }

    /**
     * A communication schedule defines time and recurrence for reading a particular set of meter data from a device or group of devices.
     *
     * @Summary Fetch a set of communication schedules
     *
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @param queryParameters queryParameters

     * @return a sorted, pageable list of elements. Only fields mentioned in field-param will be provided, or all fields if no
     * field-param was provided. The list will be sorted according to db order.
     */
    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed(Privileges.Constants.PUBLIC_REST_API)
    public PagedInfoList<ComScheduleInfo> getComSchedules(@BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters) {
        List<ComScheduleInfo> infos = comScheduleService.findAllSchedules().from(queryParameters).stream()
                .map(ct -> comScheduleInfoFactory.from(ct, uriInfo, fieldSelection.getFields()))
                .collect(toList());
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(ComScheduleResource.class);
        return PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo);
    }

    /**
     * List the fields available on this type of entity.
     * <br>E.g.
     * <br>[
     * <br> "id",
     * <br> "name",
     * <br> "actions",
     * <br> "batch"
     * <br>]
     * <br>Fields in the list can be used as parameter on a GET request to the same resource, e.g.
     * <br> <i></i>GET ..../resource?fields=id,name,batch</i>
     * <br> The call above will return only the requested fields of the entity. In the absence of a field list, all fields
     * will be returned. If IDs are required in the URL for parent entities, then will be ignored when using the PROPFIND method.
     *
     * @summary List the fields available on this type of entity
     * @return A list of field names that can be requested as parameter in the GET method on this entity type
     */
    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed(Privileges.Constants.PUBLIC_REST_API)
    public List<String> getFields() {
        return comScheduleInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }

}
