package com.elster.jupiter.time.rest.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.time.security.Privileges;
import com.elster.jupiter.time.RelativeDate;
import com.elster.jupiter.time.RelativeOperation;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.RelativePeriodCategory;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.rest.RelativeDatePreviewInfo;
import com.elster.jupiter.time.rest.RelativePeriodInfo;
import com.elster.jupiter.time.rest.RelativePeriodInfos;
import com.elster.jupiter.time.rest.RelativePeriodPreviewInfo;
import com.elster.jupiter.time.rest.impl.i18n.MessageSeeds;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.conditions.*;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/relativeperiods")
public class RelativePeriodResource {

    private static final Logger LOGGER = Logger.getLogger(RelativePeriodResource.class.getName());

    private final TimeService timeService;
    private final RestQueryService restQueryService;
    private final TransactionService transactionService;
    private final Thesaurus thesaurus;

    @Inject
    public RelativePeriodResource(TimeService timeService, RestQueryService restQueryService, TransactionService transactionService, Thesaurus thesaurus) {
        this.timeService = timeService;
        this.restQueryService = restQueryService;
        this.transactionService = transactionService;
        this.thesaurus = thesaurus;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    // not protected by privileges yet because a combobox containing all the relative periods needs to be shown when creating an export task
    public RelativePeriodInfos getRelativePeriods(@Context UriInfo uriInfo) {

        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        Query<? extends RelativePeriod> query = timeService.getRelativePeriodQuery();
        List<String> category = queryParameters.get("category");
        Condition condition = Condition.TRUE;
        if(category !=null && !category.isEmpty()){
            condition = Where.where("relativePeriodCategoryUsages.relativePeriodCategory.name").isEqualTo(category.get(0));
        }
        query.setRestriction(condition);
        RestQuery<? extends RelativePeriod> restQuery = restQueryService.wrap(query);
        List<? extends RelativePeriod> relativePeriods = restQuery.select(queryParameters, Order.ascending("name").toUpperCase());
        relativePeriods.sort((r1, r2) -> r1.getName().compareToIgnoreCase(r2.getName()));
        RelativePeriodInfos relativePeriodInfos = new RelativePeriodInfos(queryParameters.clipToLimit(relativePeriods), thesaurus);
        relativePeriodInfos.total = queryParameters.determineTotal(relativePeriods.size());

        return relativePeriodInfos;
    }

    @Path("/{id}")
    @GET
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_RELATIVE_PERIOD, Privileges.Constants.VIEW_RELATIVE_PERIOD})
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public RelativePeriodInfo getRelativePeriod(@PathParam("id") long id) {
        return new RelativePeriodInfo(getRelativePeriodOrThrowException(id), thesaurus);
    }

    @POST
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_RELATIVE_PERIOD)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response createRelativePeriod(RelativePeriodInfo relativePeriodInfo) {
        RelativeDate relativeDateFrom = new RelativeDate(relativePeriodInfo.from.convertToRelativeOperations());
        RelativeDate relativeDateTo = new RelativeDate(relativePeriodInfo.to.convertToRelativeOperations());
        verifyDateRangeOrThrowException(relativeDateFrom, relativeDateTo);
        List<RelativePeriodCategory> categories = getRelativePeriodCategoriesList(relativePeriodInfo);
        RelativePeriod period;
        try (TransactionContext context = transactionService.getContext()) {
            period = timeService.createRelativePeriod(relativePeriodInfo.name, relativeDateFrom, relativeDateTo, categories);
            context.commit();
        }
        return Response.status(Response.Status.CREATED).entity(new RelativePeriodInfo(period, thesaurus)).build();
    }

    @PUT
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_RELATIVE_PERIOD)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public RelativePeriodInfo setRelativePeriod(@PathParam("id") long id, RelativePeriodInfo relativePeriodInfo) {
        RelativePeriod relativePeriod = getRelativePeriodOrThrowException(id);
        RelativeDate relativeDateFrom = new RelativeDate(relativePeriodInfo.from.convertToRelativeOperations());
        RelativeDate relativeDateTo = new RelativeDate(relativePeriodInfo.to.convertToRelativeOperations());
        verifyDateRangeOrThrowException(relativeDateFrom, relativeDateTo);
        List<RelativePeriodCategory> categories = getRelativePeriodCategoriesList(relativePeriodInfo);
        RelativePeriod period;
        try (TransactionContext context = transactionService.getContext()) {
            period = timeService.updateRelativePeriod(relativePeriod.getId(), relativePeriodInfo.name, relativeDateFrom, relativeDateTo, categories);
            context.commit();
        }
        return new RelativePeriodInfo(period, thesaurus);
    }

    @Path("/{id}")
    @DELETE
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_RELATIVE_PERIOD)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response removeRelativePeriod(@PathParam("id") long id) {
        try {
            RelativePeriod relativePeriod = getRelativePeriodOrThrowException(id);
            List<RelativePeriodCategory> categories = relativePeriod.getRelativePeriodCategories();
            try (TransactionContext context = transactionService.getContext()) {
                for (RelativePeriodCategory category : categories) {
                    relativePeriod.removeRelativePeriodCategory(category);
                }
                relativePeriod.delete();
                context.commit();
            }
            return Response.status(Response.Status.OK).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new WebApplicationException(e.getLocalizedMessage(), Response.status(Response.Status.PRECONDITION_FAILED).entity(e.getLocalizedMessage()).build());
        }
    }

    private RelativePeriod getRelativePeriodOrThrowException(long id) {
        return timeService.findRelativePeriod(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    @Path("/{id}/preview")
    @PUT
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_RELATIVE_PERIOD, Privileges.Constants.VIEW_RELATIVE_PERIOD})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public RelativePeriodPreviewInfo pregviewRelativePeriod(@PathParam("id") long id, RelativeDatePreviewInfo relativeDatePreviewInfo) {
        RelativePeriod relativePeriod = getRelativePeriodOrThrowException(id);
        ZonedDateTime referenceDate = getZonedDateTime(relativeDatePreviewInfo);
        ZonedDateTime start = relativePeriod.getRelativeDateFrom().getRelativeDate(referenceDate);
        ZonedDateTime end = relativePeriod.getRelativeDateTo().getRelativeDate(referenceDate);
        return new RelativePeriodPreviewInfo(start, end);
    }

    @Path("/preview")
    @PUT
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_RELATIVE_PERIOD, Privileges.Constants.VIEW_RELATIVE_PERIOD})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
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
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_RELATIVE_PERIOD, Privileges.Constants.VIEW_RELATIVE_PERIOD})
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public RelativePeriodCategoryInfos getCategories(@Context UriInfo uriInfo) {
        return new RelativePeriodCategoryInfos(timeService.getRelativePeriodCategories(), thesaurus);
    }

    @Path("/weekstarts")
    @GET
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_RELATIVE_PERIOD, Privileges.Constants.VIEW_RELATIVE_PERIOD})
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response getFirstDayOfWeek() {
        return Response.ok(WeekFields.of(Locale.getDefault()).getFirstDayOfWeek().getValue()).build();
    }

    private ZonedDateTime getZonedDateTime(RelativeDatePreviewInfo relativeDatePreviewInfo) {
        Instant instant = Instant.ofEpochMilli(relativeDatePreviewInfo.date);
        ZoneId zoneId = ZoneId.ofOffset("", ZoneOffset.ofHoursMinutes(relativeDatePreviewInfo.parseOffsetHours(), relativeDatePreviewInfo.parseOffsetMinutes()));
        return ZonedDateTime.ofInstant(instant, zoneId);
    }

    private void verifyDateRangeOrThrowException(RelativeDate relativeDateFrom, RelativeDate relativeDateTo) {
        ZonedDateTime time = ZonedDateTime.now();
        if (relativeDateFrom.getRelativeDate(time).isAfter(relativeDateTo.getRelativeDate(time))) {
            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_RANGE, "to");
        }
    }

    private List<RelativePeriodCategory> getRelativePeriodCategoriesList(RelativePeriodInfo relativePeriodInfo) {
        List<RelativePeriodCategory> categories = new ArrayList<>();
        if (relativePeriodInfo.categories != null) {
            relativePeriodInfo.categories.stream().forEach(category -> {
                Optional<RelativePeriodCategory> relativePeriodCategoryRef = timeService.findRelativePeriodCategory(category.id);
                if (relativePeriodCategoryRef.isPresent()) {
                    categories.add(relativePeriodCategoryRef.get());
                }
            });
        }
        return categories;
    }
}
