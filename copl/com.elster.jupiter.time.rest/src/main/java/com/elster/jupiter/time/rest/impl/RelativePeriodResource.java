/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time.rest.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.JSONQueryValidator;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.ListPager;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.rest.util.RestValidationBuilder;
import com.elster.jupiter.time.CannotRemoveUsedCategoryException;
import com.elster.jupiter.time.RelativeDate;
import com.elster.jupiter.time.RelativeOperation;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.RelativePeriodCategory;
import com.elster.jupiter.time.RelativePeriodUsageInfo;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.rest.RelativeDatePreviewInfo;
import com.elster.jupiter.time.rest.RelativePeriodInfo;
import com.elster.jupiter.time.rest.RelativePeriodInfos;
import com.elster.jupiter.time.rest.RelativePeriodPreviewInfo;
import com.elster.jupiter.time.rest.impl.i18n.MessageSeeds;
import com.elster.jupiter.time.security.Privileges;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Where;

import com.google.common.collect.Range;

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
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Path("/relativeperiods")
public class RelativePeriodResource {

    private static final Logger LOGGER = Logger.getLogger(RelativePeriodResource.class.getName());

    private final TimeService timeService;
    private final RestQueryService restQueryService;
    private final TransactionService transactionService;
    private final Thesaurus thesaurus;
    private final ConcurrentModificationExceptionFactory conflictFactory;

    @Inject
    public RelativePeriodResource(TimeService timeService, RestQueryService restQueryService, TransactionService transactionService, Thesaurus thesaurus, ConcurrentModificationExceptionFactory conflictFactory) {
        this.timeService = timeService;
        this.restQueryService = restQueryService;
        this.transactionService = transactionService;
        this.thesaurus = thesaurus;
        this.conflictFactory = conflictFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    // not protected by privileges yet because a combobox containing all the relative periods needs to be shown when creating an export task
    public RelativePeriodInfos getRelativePeriods(@Context UriInfo uriInfo) {

        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        Query<? extends RelativePeriod> query = timeService.getRelativePeriodQuery();
        List<String> category = queryParameters.get("category");
        if(category != null && !category.isEmpty()){
            query.setRestriction(Where.where("relativePeriodCategoryUsages.relativePeriodCategory.name").isEqualTo(category.get(0)));
        }
        RestQuery<? extends RelativePeriod> restQuery = restQueryService.wrap(query);
        List<? extends RelativePeriod> relativePeriods = restQuery.select(queryParameters, Order.ascending("name").toUpperCase());
        RelativePeriodInfos relativePeriodInfos = new RelativePeriodInfos(queryParameters.clipToLimit(relativePeriods));
        relativePeriodInfos.total = queryParameters.determineTotal(relativePeriods.size());

        return relativePeriodInfos;
    }

    @Path("/{id}")
    @GET
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_RELATIVE_PERIOD, Privileges.Constants.VIEW_RELATIVE_PERIOD})
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public RelativePeriodInfo getRelativePeriod(@PathParam("id") long id) {
        return RelativePeriodInfo.withCategories(getRelativePeriodOrThrowException(id));
    }


    @Path("/{id}/usage")
    @GET
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_RELATIVE_PERIOD, Privileges.Constants.VIEW_RELATIVE_PERIOD})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getUsage(@PathParam("id") long id, @BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter filter) {
        JSONQueryValidator.validateJSONQueryParameters(queryParameters);
        Set<String> categories = filter.getLongList("category")
                .stream()
                .map(this::getCategoryName)
                .collect(Collectors.toSet());

        return PagedInfoList.fromPagedList("usage", ListPager.of(getUsageInfos(id, categories))
                .from(queryParameters)
                .find(), queryParameters);
    }

    private List<RelativePeriodUsageInfo> getUsageInfos(long id, Set<String> categories) {

        List<RelativePeriodUsageInfo> usageInfos = timeService.getTaskProviders()
                .stream()
                .filter(provider -> categories.isEmpty() || categories.contains(provider.getType()))
                .map(provider -> provider.getUsageReferences(id))
                .flatMap(List::stream)
                .sorted(Comparator.comparing(RelativePeriodUsageInfo::getTask, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());

        return usageInfos;
    }

    private String getCategoryName(long categoryId){
        return timeService.findRelativePeriodCategory(categoryId)
                .map(RelativePeriodCategory::getName)
                .orElse(null);
    }

    @POST
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_RELATIVE_PERIOD)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response createRelativePeriod(RelativePeriodInfo relativePeriodInfo) {
        RelativeDate relativeDateFrom = new RelativeDate(relativePeriodInfo.from.convertToRelativeOperations());
        RelativeDate relativeDateTo = new RelativeDate(relativePeriodInfo.to.convertToRelativeOperations());
        List<RelativePeriodCategory> categories = getRelativePeriodCategoriesList(relativePeriodInfo);
        RelativePeriod period;
        try (TransactionContext context = transactionService.getContext()) {
            period = timeService.createRelativePeriod(relativePeriodInfo.name, relativeDateFrom, relativeDateTo, categories);
            context.commit();
        }
        return Response.status(Response.Status.CREATED).entity(RelativePeriodInfo.withCategories(period)).build();
    }

    @PUT
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_RELATIVE_PERIOD)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{id}")
    public RelativePeriodInfo setRelativePeriod(@PathParam("id") long id, RelativePeriodInfo relativePeriodInfo, @BeanParam JsonQueryFilter filter) {
        Set<String> categoriesUsage = filter.getLongList("category")
                .stream()
                .map(this::getCategoryName)
                .collect(Collectors.toSet());

        //verify if you can remove a category
        validateCategoryEdit(id, categoriesUsage, relativePeriodInfo);

        relativePeriodInfo.id = id;

        try (TransactionContext context = transactionService.getContext()) {
            RelativePeriod relativePeriod = findRelativePeriodAndLock(relativePeriodInfo);
            RelativeDate relativeDateFrom = new RelativeDate(relativePeriodInfo.from.convertToRelativeOperations());
            RelativeDate relativeDateTo = new RelativeDate(relativePeriodInfo.to.convertToRelativeOperations());
            List<RelativePeriodCategory> categories = getRelativePeriodCategoriesList(relativePeriodInfo);
            RelativePeriod period;
            period = timeService.updateRelativePeriod(relativePeriod.getId(), relativePeriodInfo.name, relativeDateFrom, relativeDateTo, categories);
            context.commit();
            return RelativePeriodInfo.withCategories(period);
        }
    }

    private void validateCategoryEdit(long id, Set<String> categories, RelativePeriodInfo relativePeriodChanged) {

        List<RelativePeriodCategory> relativePeriodCategoriesOriginal = getRelativePeriodOrThrowException(id).getRelativePeriodCategories();
        List<RelativePeriodCategory> relativePeriodCategory = getRelativePeriodCategoriesList(relativePeriodChanged);
        List<RelativePeriodCategory> removedCategories = new ArrayList<>();
        List<RelativePeriodUsageInfo> categoriesUsage = getUsageInfos(id, categories);

        for (RelativePeriodCategory categoryOriginal : relativePeriodCategoriesOriginal) {
            if (!relativePeriodCategory.stream()
                    .anyMatch(category -> category.getName().equals(categoryOriginal.getName()))) {
                removedCategories.add(categoryOriginal);
            }
        }

        for (RelativePeriodCategory removed : removedCategories) {
            if (categoriesUsage.stream().anyMatch(category -> category.getType().equals(removed.getDisplayName()))) {
                throw new CannotRemoveUsedCategoryException(removed.getDisplayName(), thesaurus, MessageSeeds.CATEGORY_IN_USE);
            }
        }

    }

    @Path("/{id}")
    @DELETE
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_RELATIVE_PERIOD)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response removeRelativePeriod(@PathParam("id") long id, RelativePeriodInfo info) {
        info.id = id;
        try (TransactionContext context = transactionService.getContext()) {
            RelativePeriod relativePeriod = findRelativePeriodAndLock(info);
            List<RelativePeriodCategory> categories = relativePeriod.getRelativePeriodCategories();
            for (RelativePeriodCategory category : categories) {
                relativePeriod.removeRelativePeriodCategory(category);
            }
            relativePeriod.delete();
            context.commit();
            return Response.status(Response.Status.OK).build();
        } catch (WebApplicationException | LocalizedException e) {
            throw e;
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new WebApplicationException(e.getLocalizedMessage(), Response.status(Response.Status.PRECONDITION_FAILED).entity(e.getLocalizedMessage()).build());
        }
    }

    private RelativePeriod getRelativePeriodOrThrowException(long id) {
        return timeService.findRelativePeriod(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    private RelativePeriod findRelativePeriodAndLock(RelativePeriodInfo info) {
        return timeService.findAndLockRelativePeriodByIdAndVersion(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> timeService.findRelativePeriod(info.id).map(RelativePeriod::getVersion).orElse(null))
                        .supplier());
    }

    @Path("/{id}/preview")
    @PUT
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_RELATIVE_PERIOD, Privileges.Constants.VIEW_RELATIVE_PERIOD})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public RelativePeriodPreviewInfo previewRelativePeriod(@PathParam("id") long id, RelativeDatePreviewInfo relativeDatePreviewInfo) {
        RelativePeriod relativePeriod = getRelativePeriodOrThrowException(id);
        new RestValidationBuilder()
                .notEmpty(relativeDatePreviewInfo.date, "date")
                .validate();
        ZonedDateTime referenceDate = getZonedDateTime(relativeDatePreviewInfo);
        Range<Instant> interval = relativePeriod.getClosedOpenInterval(referenceDate);
        return new RelativePeriodPreviewInfo(interval.lowerEndpoint(), interval.upperEndpoint(), referenceDate.getZone());
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
        return new RelativePeriodCategoryInfos(timeService.getRelativePeriodCategories(),thesaurus);
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

    private List<RelativePeriodCategory> getRelativePeriodCategoriesList(RelativePeriodInfo relativePeriodInfo) {
        List<RelativePeriodCategory> categories = new ArrayList<>();
        if (relativePeriodInfo.categories != null) {
            relativePeriodInfo.categories.forEach(category -> {
                Optional<RelativePeriodCategory> relativePeriodCategoryRef = timeService.findRelativePeriodCategory(category.id);
                relativePeriodCategoryRef.ifPresent(categories::add);
            });
        }
        return categories;
    }
}
