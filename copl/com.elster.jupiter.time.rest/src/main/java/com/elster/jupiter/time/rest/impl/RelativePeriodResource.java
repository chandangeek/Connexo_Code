package com.elster.jupiter.time.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.time.*;
import com.elster.jupiter.time.rest.RelativeDatePreviewInfo;
import com.elster.jupiter.time.rest.RelativePeriodInfo;
import com.elster.jupiter.time.rest.RelativePeriodPreviewInfo;
import com.elster.jupiter.time.rest.impl.i18n.MessageSeeds;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Path("/relativeperiod")
public class RelativePeriodResource {
    private final TimeService timeService;

    @Inject
    public RelativePeriodResource(TimeService timeService) {
        this.timeService = timeService;
    }

    @GET
    @RolesAllowed(Privileges.VIEW_RELATIVE_PERIOD)
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getRelativePeriods(@BeanParam QueryParameters queryParameters) {
        List<RelativePeriod> relativePeriods = ListPager.of(timeService.getRelativePeriods(),
                (r1, r2) -> r1.getName().compareToIgnoreCase(r2.getName())).from(queryParameters).find();
        List<RelativePeriodInfo> relativePeriodInfos = new ArrayList<>();
        relativePeriods.stream().forEach(rp -> relativePeriodInfos.add(new RelativePeriodInfo(rp)));
        return PagedInfoList.asJson("data", relativePeriodInfos, queryParameters);
    }

    @Path("/{id}")
    @GET
    @RolesAllowed(Privileges.VIEW_RELATIVE_PERIOD)
    @Produces(MediaType.APPLICATION_JSON)
    public RelativePeriodInfo getRelativePeriod(@PathParam("id") long id) {
        return new RelativePeriodInfo(getRelativePeriodOrThrowException(id));
    }

    @POST
    @RolesAllowed(Privileges.CREATE_RELATIVE_PERIOD)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createRelativePeriod(RelativePeriodInfo relativePeriodInfo) {
        RelativeDate relativeDateFrom = new RelativeDate(relativePeriodInfo.from.convertToRelativeOperations());
        RelativeDate relativeDateTo = new RelativeDate(relativePeriodInfo.to.convertToRelativeOperations());
        verifyDateRangeOrThrowException(relativeDateFrom, relativeDateTo);
        RelativePeriod period = timeService.createRelativePeriod(relativePeriodInfo.name, relativeDateFrom, relativeDateTo);
        return Response.status(Response.Status.CREATED).entity(new RelativePeriodInfo(period)).build();
    }

    @PUT
    @RolesAllowed(Privileges.UPDATE_RELATIVE_PERIOD)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public RelativePeriodInfo setRelativePeriod(@PathParam("id") long id, RelativePeriodInfo relativePeriodInfo) {
        RelativePeriod relativePeriod = getRelativePeriodOrThrowException(id);
        RelativeDate relativeDateFrom = new RelativeDate(relativePeriodInfo.from.convertToRelativeOperations());
        RelativeDate relativeDateTo = new RelativeDate(relativePeriodInfo.to.convertToRelativeOperations());
        verifyDateRangeOrThrowException(relativeDateFrom, relativeDateTo);
        RelativePeriod period = timeService.updateRelativePeriod(relativePeriod.getId(), relativePeriodInfo.name, relativeDateFrom, relativeDateTo);
        return new RelativePeriodInfo(period);
    }

    @Path("/{id}")
    @DELETE
    @RolesAllowed(Privileges.DELETE_RELATIVE_PERIOD)
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeRelativePeriod(@PathParam("id") long id) {
        RelativePeriod relativePeriod = getRelativePeriodOrThrowException(id);
        relativePeriod.delete();
        return Response.status(Response.Status.OK).build();
    }

    private RelativePeriod getRelativePeriodOrThrowException(long id) {
        RelativePeriod relativePeriod = timeService.findRelativePeriod(id);
        if(relativePeriod == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return relativePeriod;
    }

    @Path("/{id}/preview")
    @PUT
    @RolesAllowed(Privileges.VIEW_RELATIVE_PERIOD)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public RelativePeriodPreviewInfo previewRelativePeriod(@PathParam("id") long id, RelativeDatePreviewInfo relativeDatePreviewInfo) {
        RelativePeriod relativePeriod = getRelativePeriodOrThrowException(id);
        ZonedDateTime referenceDate = getZonedDateTime(relativeDatePreviewInfo);
        ZonedDateTime start = relativePeriod.getRelativeDateFrom().getRelativeDate(referenceDate);
        ZonedDateTime end = relativePeriod.getRelativeDateTo().getRelativeDate(referenceDate);
        return new RelativePeriodPreviewInfo(start, end);
    }

    @Path("/preview")
    @PUT
    @RolesAllowed(Privileges.VIEW_RELATIVE_PERIOD)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public RelativeDatePreviewInfo previewRelativeDate(RelativeDatePreviewInfo relativeDatePreviewInfo) {
        List<RelativeOperation> operations = new ArrayList<>();
        ZonedDateTime referenceDate = getZonedDateTime(relativeDatePreviewInfo);
        if (relativeDatePreviewInfo.relativeDateInfo != null) {
            operations = relativeDatePreviewInfo.relativeDateInfo.convertToRelativeOperations();
        }
        RelativeDate relativeDate = new RelativeDate(operations);
        ZonedDateTime target = relativeDate.getRelativeDate(referenceDate);
        return new RelativeDatePreviewInfo(target);
    }


    @Path("/categories")
    @GET
    @RolesAllowed(Privileges.VIEW_RELATIVE_PERIOD)
    @Produces(MediaType.APPLICATION_JSON)
    public List<RelativePeriodCategoryInfo> getCategories() {
        List<RelativePeriodCategoryInfo> categoryInfos = new ArrayList<>();
        categoryInfos.addAll(RelativePeriodCategoryInfo.from(timeService.getRelativePeriodCategories()));
        return categoryInfos;
    }

    private ZonedDateTime getZonedDateTime(RelativeDatePreviewInfo relativeDatePreviewInfo) {
        Instant instant = Instant.ofEpochMilli(relativeDatePreviewInfo.referenceDate);
        ZoneId zoneId = ZoneId.ofOffset("", ZoneOffset.ofHoursMinutes(relativeDatePreviewInfo.getOffsetHours(), relativeDatePreviewInfo.getOffsetMinutes()));
        return ZonedDateTime.ofInstant(instant, zoneId);
    }

    private void verifyDateRangeOrThrowException(RelativeDate relativeDateFrom, RelativeDate relativeDateTo) {
        ZonedDateTime time = ZonedDateTime.now();
        if(relativeDateFrom.getRelativeDate(time).isAfter(relativeDateTo.getRelativeDate(time))) {
            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_RANGE, "to");
        }
    }
}
