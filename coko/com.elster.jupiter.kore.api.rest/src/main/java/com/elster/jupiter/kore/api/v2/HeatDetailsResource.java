package com.elster.jupiter.kore.api.v2;

import com.elster.jupiter.kore.api.impl.MessageSeeds;
import com.elster.jupiter.kore.api.security.Privileges;
import com.elster.jupiter.metering.HeatDetail;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.UsagePointDetailBuilder;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.rest.api.util.v1.hypermedia.FieldSelection;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.PROPFIND;
import com.elster.jupiter.rest.util.Transactional;

import javax.annotation.security.RolesAllowed;
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

public class HeatDetailsResource {

    private final HeatDetailInfoFactory heatDetailInfoFactory;
    private final ExceptionFactory exceptionFactory;
    private final Clock clock;
    private final MeteringService meteringService;
    private UsagePoint usagePoint;

    @Inject
    public HeatDetailsResource(HeatDetailInfoFactory heatDetailInfoFactory, ExceptionFactory exceptionFactory, Clock clock, MeteringService meteringService) {
        this.heatDetailInfoFactory = heatDetailInfoFactory;
        this.exceptionFactory = exceptionFactory;
        this.clock = clock;
        this.meteringService = meteringService;
    }

    HeatDetailsResource init(UsagePoint usagePoint) {
        this.usagePoint = usagePoint;
        return this;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public HeatDetailInfo getCurrentHeatDetails(@PathParam("heatDetailsId") long heatDetailsId, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        return getHeatDetails(Instant.now(clock).toEpochMilli(), fieldSelection, uriInfo);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{time}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public HeatDetailInfo getHeatDetails(@PathParam("time") long time, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        HeatDetail detail = (HeatDetail) usagePoint.getDetail(Instant.ofEpochMilli(time))
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DETAIL));
        return heatDetailInfoFactory.from(detail, uriInfo, fieldSelection.getFields());
    }

    @POST
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public Response createHeatDetail(HeatDetailInfo heatDetailInfo, @Context UriInfo uriInfo) {
        if (heatDetailInfo == null || heatDetailInfo.version == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.VERSION_MISSING, "version");
        }
        meteringService.findAndLockUsagePointByIdAndVersion(usagePoint.getId(), heatDetailInfo.version)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_USAGE_POINT));
        if (heatDetailInfo.effectivity == null || heatDetailInfo.effectivity.lowerEnd == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_MISSING, "effectivity.lowerEnd");
        }
        UsagePointDetailBuilder builder = heatDetailInfoFactory.createDetail(usagePoint, heatDetailInfo);
        builder.validate();
        UsagePointDetail detail = builder.create();

        URI uri = uriInfo.getBaseUriBuilder().
                path(UsagePointResource.class).
                path(UsagePointResource.class, "getDetailsResource").
                path(HeatDetailsResource.class, "getHeatDetails").
                build(usagePoint.getMRID(), detail.getRange().lowerEndpoint());

        return Response.created(uri).build();
    }

    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public List<String> getFields() {
        return heatDetailInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }
}
