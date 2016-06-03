package com.elster.jupiter.kore.api.impl;

import com.elster.jupiter.kore.api.impl.utils.MessageSeeds;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.UsagePointDetailBuilder;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
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

public class ElectricityDetailResource {

    private final ElectricityDetailInfoFactory electricityDetailInfoFactory;
    private final ExceptionFactory exceptionFactory;
    private final Clock clock;
    private UsagePoint usagePoint;
    private final MeteringService meteringService;

    @Inject
    public ElectricityDetailResource(ElectricityDetailInfoFactory electricityDetailInfoFactory, ExceptionFactory exceptionFactory, Clock clock, MeteringService meteringService) {
        this.electricityDetailInfoFactory = electricityDetailInfoFactory;
        this.exceptionFactory = exceptionFactory;
        this.clock = clock;
        this.meteringService = meteringService;
    }

    ElectricityDetailResource init(UsagePoint usagePoint) {
        this.usagePoint = usagePoint;
        return this;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{lowerRange}")
//    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public ElectricityDetailInfo getElectricityDetails(@PathParam("lowerRange") long lowerRange, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        UsagePointDetail usagePointDetail = usagePoint.getDetail(Instant.ofEpochMilli(lowerRange))
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DETAIL));
        return electricityDetailInfoFactory.from((ElectricityDetail) usagePointDetail, uriInfo, fieldSelection.getFields());
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
//    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public ElectricityDetailInfo getCurrentElectricityDetails(@BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        return getElectricityDetails(Instant.now(clock).toEpochMilli(), fieldSelection, uriInfo);
    }

    @POST
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
//    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public Response createElectricityDetail(ElectricityDetailInfo electricityDetailInfo, @Context UriInfo uriInfo) {
        if (electricityDetailInfo.version == null) {
            exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.VERSION_MISSING, "version");
        }
        meteringService.findAndLockUsagePointByIdAndVersion(usagePoint.getId(), electricityDetailInfo.version)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_USAGE_POINT));
        if (electricityDetailInfo == null || electricityDetailInfo.effectivity == null || electricityDetailInfo.effectivity.lowerEnd == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_MISSING, "effectivity.lowerEnd");
        }
        UsagePointDetailBuilder builder = electricityDetailInfoFactory.createDetail(usagePoint, electricityDetailInfo);
        builder.validate();
        UsagePointDetail detail = builder.create();

        URI uri = uriInfo.getBaseUriBuilder().
                path(UsagePointResource.class).
                path(UsagePointResource.class, "getDetailsResource").
                path(ElectricityDetailResource.class, "getElectricityDetails").
                build(usagePoint.getId(), detail.getRange().lowerEndpoint().toEpochMilli());

        return Response.created(uri).build();
    }

    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
//    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public List<String> getFields() {
        return electricityDetailInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }


}
