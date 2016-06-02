package com.elster.jupiter.kore.api.impl;

import com.elster.jupiter.metering.HeatDetail;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.PROPFIND;
import com.elster.jupiter.rest.util.hypermedia.FieldSelection;

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
import java.time.Clock;
import java.time.Instant;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Path("/heatdetailss")
public class HeatDetailsResource {

    private final HeatDetailInfoFactory heatDetailInfoFactory;
    private final ExceptionFactory exceptionFactory;
    private final Clock clock;
    private UsagePoint usagePoint;

    @Inject
    public HeatDetailsResource(HeatDetailInfoFactory heatDetailInfoFactory, ExceptionFactory exceptionFactory, Clock clock) {
        this.heatDetailInfoFactory = heatDetailInfoFactory;
        this.exceptionFactory = exceptionFactory;
        this.clock = clock;
    }

    HeatDetailsResource init(UsagePoint usagePoint) {
        this.usagePoint = usagePoint;
        return this;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
//    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public HeatDetailInfo getCurrentHeatDetails(@PathParam("heatDetailsId") long heatDetailsId, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        return getHeatDetails(Instant.now(clock).toEpochMilli(), fieldSelection, uriInfo);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{lowerEnd}")
//    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public HeatDetailInfo getHeatDetails(@PathParam("lowerEnd") long lowerEnd, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        HeatDetail detail = (HeatDetail) usagePoint.getDetail(Instant.now(clock))
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, com.elster.jupiter.kore.api.impl.utils.MessageSeeds.NO_SUCH_DETAIL));
        return heatDetailInfoFactory.from(detail, uriInfo, fieldSelection.getFields());
    }

    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
//    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public List<String> getFields() {
        return heatDetailInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }


}
