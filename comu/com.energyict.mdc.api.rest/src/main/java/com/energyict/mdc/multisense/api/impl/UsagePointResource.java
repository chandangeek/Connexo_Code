package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.PROPFIND;
import com.energyict.mdc.multisense.api.impl.utils.FieldSelection;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.multisense.api.security.Privileges;

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
import javax.ws.rs.core.UriInfo;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Path("/usagepoints")
public class UsagePointResource {

    private final UsagePointInfoFactory usagePointInfoFactory;
    private final MeteringService meteringService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public UsagePointResource(MeteringService meteringService, UsagePointInfoFactory usagePointInfoFactory, ExceptionFactory exceptionFactory) {
        this.meteringService = meteringService;
        this.usagePointInfoFactory = usagePointInfoFactory;
        this.exceptionFactory = exceptionFactory;
    }

    /**
     * <p>A usage point is a point in the grid where data is measured (energy consumption and/or production). This point can
     * be either virtual (in order to perform calculations within the system), or physical. A physical usage point can be
     * either used to deliver a service or not in which case we speak of a service delivery point.</p>
     * <p>
     * <p>The usage point is often also refered to as a service delivery point or a point of delivery (typically UK with a
     * meter point reference number - MPRN or MPAN).</p>
     * <p>
     * <p>On a physical usage point one or multiple meters can be installed (sub metering, subtracting or control metering,
     * etc.), and these can change over time.</p>
     *
     * @param usagePointId Unique identifier of the usage point
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @return The usage point as identified
     * @summary fetch a usage point by id
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{usagePointId}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public UsagePointInfo getUsagePoint(@PathParam("usagePointId") long usagePointId, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        return meteringService.findUsagePoint(usagePointId)
                .map(ct -> usagePointInfoFactory.from(ct, uriInfo, fieldSelection.getFields()))
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_USAGE_POINT));
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
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public List<String> getFields() {
        return usagePointInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }


}
