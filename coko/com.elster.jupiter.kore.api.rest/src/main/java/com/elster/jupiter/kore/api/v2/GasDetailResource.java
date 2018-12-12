/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2;

import com.elster.jupiter.kore.api.impl.MessageSeeds;
import com.elster.jupiter.kore.api.security.Privileges;
import com.elster.jupiter.metering.GasDetail;
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

public class GasDetailResource {

    private final GasDetailInfoFactory gasDetailInfoFactory;
    private final ExceptionFactory exceptionFactory;
    private final Clock clock;
    private final MeteringService meteringService;
    private UsagePoint usagePoint;

    @Inject
    public GasDetailResource(GasDetailInfoFactory gasDetailInfoFactory, ExceptionFactory exceptionFactory, Clock clock, MeteringService meteringService) {
        this.gasDetailInfoFactory = gasDetailInfoFactory;
        this.exceptionFactory = exceptionFactory;
        this.clock = clock;
        this.meteringService = meteringService;
    }

    GasDetailResource init(UsagePoint usagePoint) {
        this.usagePoint = usagePoint;
        return this;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public GasDetailInfo getCurrentGasDetails(@BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        return getGasDetails(Instant.now(clock).toEpochMilli(), fieldSelection, uriInfo);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{time}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public GasDetailInfo getGasDetails(@PathParam("time") long time, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        UsagePointDetail usagePointDetail = usagePoint.getDetail(Instant.ofEpochMilli(time))
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_DETAIL));

        return gasDetailInfoFactory.from((GasDetail) usagePointDetail, uriInfo, fieldSelection.getFields());
    }

    @POST
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public Response createGasDetail(GasDetailInfo gasDetailInfo, @Context UriInfo uriInfo) {
        if (gasDetailInfo == null || gasDetailInfo.version == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.VERSION_MISSING, "version");
        }
        meteringService.findAndLockUsagePointByIdAndVersion(usagePoint.getId(), gasDetailInfo.version)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_USAGE_POINT));
        if (gasDetailInfo.effectivity == null || gasDetailInfo.effectivity.lowerEnd == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_MISSING, "effectivity.lowerEnd");
        }
        UsagePointDetailBuilder builder = gasDetailInfoFactory.createDetail(usagePoint, gasDetailInfo);
        builder.validate();
        UsagePointDetail detail = builder.create();

        URI uri = uriInfo.getBaseUriBuilder().
                path(UsagePointResource.class).
                path(UsagePointResource.class, "getDetailsResource").
                path(GasDetailResource.class, "getGasDetails").
                build(usagePoint.getMRID(), detail.getRange().lowerEndpoint());

        return Response.created(uri).build();
    }

    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public List<String> getFields() {
        return gasDetailInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }
}
