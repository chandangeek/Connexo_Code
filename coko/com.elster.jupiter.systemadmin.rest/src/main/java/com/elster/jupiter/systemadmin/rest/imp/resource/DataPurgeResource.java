package com.elster.jupiter.systemadmin.rest.imp.resource;

import com.elster.jupiter.data.lifecycle.LifeCycleCategory;
import com.elster.jupiter.data.lifecycle.LifeCycleCategoryKind;
import com.elster.jupiter.data.lifecycle.LifeCycleService;
import com.elster.jupiter.data.lifecycle.security.Privileges;
import com.elster.jupiter.nls.Thesaurus;
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
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Path("/data")
public class DataPurgeResource {

    private LifeCycleService lifeCycleService;
    private TaskService taskService;
    private TransactionService transactionService;
    private Thesaurus thesaurus;

    public DataPurgeResource() {
    }

    @Inject
    public void setLifeCycleService(LifeCycleService lifeCycleService) {
        this.lifeCycleService = lifeCycleService;
    }

    @Inject
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Inject
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Inject
    public void setThesaurus(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
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
            Map<LifeCycleCategoryKind, LifeCycleCategoryInfo> categoriesMap = updatedCategories.data.stream()
                    .collect(Collectors.toMap(info -> LifeCycleCategoryKind.valueOf(info.kind), Function.identity()));
            try (TransactionContext context = transactionService.getContext()) {
                lifeCycleService.getCategories().stream()
                        .filter(c -> categoriesMap.keySet().contains(c.getKind()))
                        .forEach(c -> c.setRetentionDays(categoriesMap.get(c.getKind()).retention));
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
        try (TransactionContext context = transactionService.getContext()) {
            if (updatedCategory != null) {
                lifeCycleService.getCategories().stream()
                        .filter(category -> category.getKind().name().equalsIgnoreCase(updatedCategory.kind))
                        .findFirst()
                        .ifPresent(category -> category.setRetentionDays(updatedCategory.retention));
            }
            context.commit();
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
        return Response.ok(ListInfo.from(sortedHistory.subList(start, max), PurgeHistoryInfo::new).paged(queryParameters)).build();
    }

    @GET
    @Path("/history/{id}")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DATA_PURGE, Privileges.Constants.VIEW_DATA_PURGE})
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response getPurgeHistoryRecord(@PathParam("id") long id) {
        TaskOccurrence occurrence = getTaskOccurenceOrThrowException(id);
        return Response.ok(new PurgeHistoryInfo(occurrence)).build();
    }

    @GET
    @Path("/history/{id}/categories")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DATA_PURGE, Privileges.Constants.VIEW_DATA_PURGE})
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response getPurgeHistory(@PathParam("id") long id) {
        Instant triggerTime = getTaskOccurenceOrThrowException(id).getTriggerTime();
        return Response.ok(ListInfo.from(lifeCycleService.getCategoriesAsOf(triggerTime), getCategoryInfoMapper())).build();
    }

    @GET
    @Path("/history/{id}/logs")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DATA_PURGE, Privileges.Constants.VIEW_DATA_PURGE})
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response getPurgeLogForOccurence(@PathParam("id") long id, @Context UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        TaskOccurrence occurrence = getTaskOccurenceOrThrowException(id);
        List<? extends LogEntry> logEntries = occurrence.getLogsFinder().setStart(queryParameters.getStartInt()).setLimit(queryParameters.getLimit()).find();
        return Response.ok(ListInfo.from(logEntries, PurgeHistoryLogInfo::new).paged(queryParameters)).build();
    }

    private TaskOccurrence getTaskOccurenceOrThrowException(long id) {
        Optional<TaskOccurrence> occurrenceRef = taskService.getOccurrence(id);
        if (!occurrenceRef.isPresent()) {
            throw new EntityNotFound(thesaurus, MessageSeeds.PURGE_HISTORY_DOES_NOT_EXIST, id);
        }
        return occurrenceRef.get();
    }

    private Function<LifeCycleCategory, LifeCycleCategoryInfo> getCategoryInfoMapper(){
        return c -> new LifeCycleCategoryInfo(c, thesaurus);
    }
}
