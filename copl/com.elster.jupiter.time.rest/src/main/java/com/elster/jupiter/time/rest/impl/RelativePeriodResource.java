package com.elster.jupiter.time.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.Privileges;
import com.elster.jupiter.time.RelativeDate;
import com.elster.jupiter.time.RelativeOperation;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.RelativePeriodCategory;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.rest.RelativeDatePreviewInfo;
import com.elster.jupiter.time.rest.RelativePeriodInfo;
import com.elster.jupiter.time.rest.RelativePeriodPreviewInfo;
import com.elster.jupiter.time.rest.impl.i18n.MessageSeeds;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Path("/relativeperiods")
public class RelativePeriodResource {
    private final TimeService timeService;
    private final Thesaurus thesaurus;

    @Inject
    public RelativePeriodResource(TimeService timeService, Thesaurus thesaurus) {
        this.timeService = timeService;
        this.thesaurus = thesaurus;
    }

    @GET
    @RolesAllowed(Privileges.VIEW_RELATIVE_PERIOD)
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getRelativePeriods(@BeanParam QueryParameters queryParameters) {
        List<RelativePeriod> relativePeriods = ListPager.of(timeService.getRelativePeriods(),
                (r1, r2) -> r1.getName().compareToIgnoreCase(r2.getName())).from(queryParameters).find();
        List<RelativePeriodInfo> relativePeriodInfos = new ArrayList<>();
        relativePeriods.stream().forEach(rp -> relativePeriodInfos.add(new RelativePeriodInfo(rp, thesaurus)));
        return PagedInfoList.asJson("data", relativePeriodInfos, queryParameters);
    }

    @Path("/{id}")
    @GET
    @RolesAllowed(Privileges.VIEW_RELATIVE_PERIOD)
    @Produces(MediaType.APPLICATION_JSON)
    public RelativePeriodInfo getRelativePeriod(@PathParam("id") long id) {
        return new RelativePeriodInfo(getRelativePeriodOrThrowException(id), thesaurus);
    }

    @POST
    @RolesAllowed(Privileges.CREATE_RELATIVE_PERIOD)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createRelativePeriod(RelativePeriodInfo relativePeriodInfo) {
        RelativeDate relativeDateFrom = new RelativeDate(relativePeriodInfo.from.convertToRelativeOperations());
        RelativeDate relativeDateTo = new RelativeDate(relativePeriodInfo.to.convertToRelativeOperations());
        verifyDateRangeOrThrowException(relativeDateFrom, relativeDateTo);
        List<RelativePeriodCategory> categories = getRelativePeriodCategoriesList(relativePeriodInfo);
        RelativePeriod period = timeService.createRelativePeriod(relativePeriodInfo.name, relativeDateFrom, relativeDateTo, categories);
        return Response.status(Response.Status.CREATED).entity(new RelativePeriodInfo(period, thesaurus)).build();
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
        List<RelativePeriodCategory> categories = getRelativePeriodCategoriesList(relativePeriodInfo);
        RelativePeriod period;
        try {
            period = timeService.updateRelativePeriod(relativePeriod.getId(), relativePeriodInfo.name, relativeDateFrom, relativeDateTo, categories);
        } catch (Exception ex) {
            throw new WebApplicationException(ex.getMessage());
        }
        return new RelativePeriodInfo(period, thesaurus);
    }

    @Path("/{id}")
    @DELETE
    @RolesAllowed(Privileges.DELETE_RELATIVE_PERIOD)
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeRelativePeriod(@PathParam("id") long id) {
        RelativePeriod relativePeriod = getRelativePeriodOrThrowException(id);
        List<RelativePeriodCategory> categories = relativePeriod.getRelativePeriodCategories();
        for(RelativePeriodCategory category : categories) {
            try {
                relativePeriod.removeRelativePeriodCategory(category);
            } catch (Exception ex) {
                throw new WebApplicationException(ex.getMessage());
            }
        }
        relativePeriod.delete();
        return Response.status(Response.Status.OK).build();
    }

    private RelativePeriod getRelativePeriodOrThrowException(long id) {
        return timeService.findRelativePeriod(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
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
    public Response getCategories(@BeanParam QueryParameters queryParameters) {
        List<RelativePeriodCategoryInfo> categoryInfos = new ArrayList<>();
        categoryInfos.addAll(RelativePeriodCategoryInfo.from(timeService.getRelativePeriodCategories(), thesaurus));
        return Response.ok(PagedInfoList.asJson("data", categoryInfos, queryParameters)).build();
    }

    private ZonedDateTime getZonedDateTime(RelativeDatePreviewInfo relativeDatePreviewInfo) {
        Instant instant = Instant.ofEpochMilli(relativeDatePreviewInfo.date);
        ZoneId zoneId = ZoneId.ofOffset("", ZoneOffset.ofHoursMinutes(relativeDatePreviewInfo.parseOffsetHours(), relativeDatePreviewInfo.parseOffsetMinutes()));
        return ZonedDateTime.ofInstant(instant, zoneId);
    }

    private void verifyDateRangeOrThrowException(RelativeDate relativeDateFrom, RelativeDate relativeDateTo) {
        ZonedDateTime time = ZonedDateTime.now();
        if(relativeDateFrom.getRelativeDate(time).isAfter(relativeDateTo.getRelativeDate(time))) {
            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_RANGE, "to");
        }
    }

    private List<RelativePeriodCategory> getRelativePeriodCategoriesList(RelativePeriodInfo relativePeriodInfo) {
        List<RelativePeriodCategory> categories = new ArrayList<>();
        if(relativePeriodInfo.categories != null) {
            relativePeriodInfo.categories.stream().forEach(category -> {
                Optional<RelativePeriodCategory> relativePeriodCategoryRef = timeService.findRelativePeriodCategory(category.id);
                if(relativePeriodCategoryRef.isPresent()) {
                    categories.add(relativePeriodCategoryRef.get());
                }
            });
        }
        return categories;
    }
}
