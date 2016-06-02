package com.elster.jupiter.kore.api.impl;

import com.elster.jupiter.kore.api.impl.security.Privileges;
import com.elster.jupiter.kore.api.impl.utils.MessageSeeds;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.PROPFIND;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.rest.util.hypermedia.FieldSelection;

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
import java.time.Clock;
import java.time.Instant;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class ElectricityDetailResource {

    private final ElectricityDetailInfoFactory electricityDetailInfoFactory;
    private final ExceptionFactory exceptionFactory;
    private final Clock clock;
    private UsagePoint usagePoint;

    @Inject
    public ElectricityDetailResource(ElectricityDetailInfoFactory electricityDetailInfoFactory, ExceptionFactory exceptionFactory, Clock clock) {
        this.electricityDetailInfoFactory = electricityDetailInfoFactory;
        this.exceptionFactory = exceptionFactory;
        this.clock = clock;
    }

    ElectricityDetailResource init(UsagePoint usagePoint) {
        this.usagePoint = usagePoint;
        return this;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{lowerRange}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public ElectricityDetailInfo getElectricityDetail(@PathParam("lowerRange") long lowerRange, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        UsagePointDetail usagePointDetail = usagePoint.getDetail(Instant.ofEpochMilli(lowerRange))
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DETAIL));
        return electricityDetailInfoFactory.from((ElectricityDetail) usagePointDetail, uriInfo, fieldSelection.getFields());
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public ElectricityDetailInfo getElectricityDetail(@BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        UsagePointDetail usagePointDetail = usagePoint.getDetail(Instant.now(clock))
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DETAIL));
        return electricityDetailInfoFactory.from((ElectricityDetail) usagePointDetail, uriInfo, fieldSelection.getFields());
    }

    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public List<String> getFields() {
        return electricityDetailInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }


}
