/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.systemadmin.rest.imp.resource;

import com.elster.jupiter.data.lifecycle.LifeCycleCategory;
import com.elster.jupiter.data.lifecycle.LifeCycleCategoryKind;
import com.elster.jupiter.data.lifecycle.LifeCycleService;
import com.elster.jupiter.data.lifecycle.security.Privileges;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.systemadmin.rest.imp.response.LifeCycleCategoryInfo;
import com.elster.jupiter.systemadmin.rest.imp.response.ListInfo;
import com.elster.jupiter.systemadmin.rest.imp.response.PurgeHistoryInfo;
import com.elster.jupiter.systemadmin.rest.imp.response.PurgeHistoryLogInfo;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.logging.LogEntry;

import com.google.common.collect.Range;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Path("/data")
public class DataPurgeResource {

    private final LifeCycleService lifeCycleService;
    private final TaskService taskService;
    private final TransactionService transactionService;
    private final Thesaurus thesaurus;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final Clock clock;

    @Inject
    public DataPurgeResource(LifeCycleService lifeCycleService,
                             TaskService taskService,
                             TransactionService transactionService,
                             Thesaurus thesaurus,
                             ConcurrentModificationExceptionFactory conflictFactory, Clock clock) {
        this.lifeCycleService = lifeCycleService;
        this.taskService = taskService;
        this.transactionService = transactionService;
        this.thesaurus = thesaurus;
        this.conflictFactory = conflictFactory;
        this.clock = clock;
    }

    @GET
    @Path("/lifecycle/categories")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DATA_PURGE, Privileges.Constants.VIEW_DATA_PURGE})
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response getLifeCycleCategories() {
        return Response.ok(ListInfo.from(lifeCycleService.getCategories(),  getCategoryInfoMapper())).build();
    }

    @PUT
    @Path("/lifecycle/categories")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DATA_PURGE)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response updateLifeCycleCategories(ListInfo<LifeCycleCategoryInfo> updatedCategories) {
        if (updatedCategories != null) {
            try (TransactionContext context = transactionService.getContext()) {
                updatedCategories.data.stream().forEach(info -> findAndLockLifeCycleCategory(info).setRetentionDays(info.retention));
                context.commit();
            }
        }
        return Response.ok(ListInfo.from(lifeCycleService.getCategories(), getCategoryInfoMapper())).build();
    }

    @PUT
    @Path("/lifecycle/categories/{key}")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DATA_PURGE)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response updateLifeCycleCategory(LifeCycleCategoryInfo updatedCategory) {
        if (updatedCategory != null) {
            try (TransactionContext context = transactionService.getContext()) {
                findAndLockLifeCycleCategory(updatedCategory).setRetentionDays(updatedCategory.retention);
                context.commit();
            }
        }
        return Response.ok(ListInfo.from(lifeCycleService.getCategories(), getCategoryInfoMapper())).build();
    }

    @GET
    @Path("/history")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DATA_PURGE, Privileges.Constants.VIEW_DATA_PURGE})
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response getPurgeHistory(@Context UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<TaskOccurrence> sortedHistory = taskService.getOccurrences(lifeCycleService.getTask(), Range.all()).stream()
                .sorted(Comparator.comparing(o -> o.getStartDate().orElse(Instant.EPOCH))).collect(Collectors.toList());
        Collections.reverse(sortedHistory);
        int start = queryParameters.getStartInt() < sortedHistory.size() ? queryParameters.getStartInt() : sortedHistory.size();
        int max = queryParameters.getLimit() + start + 1;
        if (max > sortedHistory.size()){
            max = sortedHistory.size();
        }
        if (start > max){
            max = start;
        }
        return Response.ok(ListInfo.from(sortedHistory.subList(start, max), occurrence -> new PurgeHistoryInfo(occurrence, clock)).paged(queryParameters)).build();
    }

    @GET
    @Path("/history/{id}")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DATA_PURGE, Privileges.Constants.VIEW_DATA_PURGE})
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response getPurgeHistoryRecord(@PathParam("id") long id) {
        TaskOccurrence occurrence = getTaskOccurrenceOrThrowException(id);
        return Response.ok(new PurgeHistoryInfo(occurrence, clock)).build();
    }

    @GET
    @Path("/history/{id}/categories")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DATA_PURGE, Privileges.Constants.VIEW_DATA_PURGE})
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response getPurgeHistory(@PathParam("id") long id) {
        Instant triggerTime = getTaskOccurrenceOrThrowException(id).getTriggerTime();
        return Response.ok(ListInfo.from(lifeCycleService.getCategoriesAsOf(triggerTime), getCategoryInfoMapper())).build();
    }

    @GET
    @Path("/history/{id}/logs")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DATA_PURGE, Privileges.Constants.VIEW_DATA_PURGE})
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response getPurgeLogForOccurence(@PathParam("id") long id, @Context UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        TaskOccurrence occurrence = getTaskOccurrenceOrThrowException(id);
        List<? extends LogEntry> logEntries = occurrence.getLogsFinder().setStart(queryParameters.getStartInt()).setLimit(queryParameters.getLimit()).find();
        return Response.ok(ListInfo.from(logEntries, PurgeHistoryLogInfo::new).paged(queryParameters)).build();
    }

    private TaskOccurrence getTaskOccurrenceOrThrowException(long id) {
        Optional<TaskOccurrence> occurrenceRef = taskService.getOccurrence(id);
        if (!occurrenceRef.isPresent()) {
            throw new EntityNotFound(thesaurus, MessageSeeds.PURGE_HISTORY_DOES_NOT_EXIST, id);
        }
        return occurrenceRef.get();
    }

    private Function<LifeCycleCategory, LifeCycleCategoryInfo> getCategoryInfoMapper(){
        return LifeCycleCategoryInfo::new;
    }

    private LifeCycleCategory findAndLockLifeCycleCategory(LifeCycleCategoryInfo info) {
        return this.lifeCycleService.findAndLockCategoryByKeyAndVersion(LifeCycleCategoryKind.valueOf(info.kind), info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> lifeCycleService.getCategories()
                                .stream()
                                .filter(category -> category.getKind().name().equalsIgnoreCase(info.kind))
                                .findFirst()
                                .map(LifeCycleCategory::getVersion).orElse(null))
                        .supplier());
    }
}
