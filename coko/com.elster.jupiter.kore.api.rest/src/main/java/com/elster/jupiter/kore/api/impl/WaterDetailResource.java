package com.elster.jupiter.kore.api.impl;

import com.elster.jupiter.kore.api.impl.utils.MessageSeeds;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.UsagePointDetailBuilder;
import com.elster.jupiter.metering.WaterDetail;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.PROPFIND;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.rest.util.hypermedia.FieldSelection;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class WaterDetailResource {

    private final WaterDetailInfoFactory waterDetailInfoFactory;
    private final ExceptionFactory exceptionFactory;
    private UsagePoint usagePoint;
    private final Clock clock;

    @Inject
    public WaterDetailResource(WaterDetailInfoFactory waterDetailInfoFactory, ExceptionFactory exceptionFactory, Clock clock) {
        this.waterDetailInfoFactory = waterDetailInfoFactory;
        this.exceptionFactory = exceptionFactory;
        this.clock = clock;
    }

    WaterDetailResource init(UsagePoint usagePoint) {
        this.usagePoint = usagePoint;
        return this;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
//    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public WaterDetailInfo getCurrentWaterDetail(@BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        return getWaterDetail(Instant.now(clock).toEpochMilli(), fieldSelection, uriInfo);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{lowerEnd}")
//    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public WaterDetailInfo getWaterDetail(@PathParam("lowerEnd") long lowerEnd, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        WaterDetail detail = (WaterDetail) usagePoint.getDetail(Instant.ofEpochMilli(lowerEnd))
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DETAIL));
        return waterDetailInfoFactory.from(detail, uriInfo, fieldSelection.getFields());
    }

    @POST
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
//    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public Response createWaterDetail(WaterDetailInfo waterDetailInfo, @Context UriInfo uriInfo) {
        UsagePointDetailBuilder builder = waterDetailInfoFactory.createDetail(usagePoint, waterDetailInfo);
        builder.validate();
        UsagePointDetail detail = builder.create();

        URI uri = uriInfo.getBaseUriBuilder().
                path(UsagePointResource.class).
                path(UsagePointResource.class, "getDetailsResource").
                path(WaterDetailResource.class, "getWaterDetails").
                build(usagePoint.getId(), detail.getRange().lowerEndpoint());

        return Response.created(uri).build();
    }

    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
//    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public List<String> getFields() {
        return waterDetailInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }


}
