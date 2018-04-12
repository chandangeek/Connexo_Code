/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.customtask.rest.impl;


import com.elster.jupiter.customtask.CustomTask;
import com.elster.jupiter.customtask.CustomTaskBuilder;
import com.elster.jupiter.customtask.CustomTaskOccurrence;
import com.elster.jupiter.customtask.CustomTaskOccurrenceFinder;
import com.elster.jupiter.customtask.CustomTaskService;
import com.elster.jupiter.customtask.CustomTaskStatus;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.tasks.security.Privileges;
import com.elster.jupiter.transaction.CommitException;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.logging.LogEntry;
import com.elster.jupiter.util.logging.LogEntryFinder;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.util.time.ScheduleExpression;

import com.google.common.collect.Range;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Path("/customtask")
public class CustomTaskResource {

    static final String X_CONNEXO_APPLICATION_NAME = "X-CONNEXO-APPLICATION-NAME";

    private final CustomTaskService customTaskService;
    private final TaskService taskService;
    private final Thesaurus thesaurus;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final CustomTaskInfoFactory customTaskInfoFactory;
    private final PropertyValueInfoService propertyValueInfoService;
    private final CustomTaskHistoryInfoFactory customTaskHistoryInfoFactory;
    private final Clock clock;

    @Inject
    public CustomTaskResource(CustomTaskService customTaskService, TaskService taskService, Thesaurus thesaurus,
                              ConcurrentModificationExceptionFactory conflictFactory,
                              CustomTaskInfoFactory customTaskInfoFactory,
                              PropertyValueInfoService propertyValueInfoService,
                              CustomTaskHistoryInfoFactory customTaskHistoryInfoFactory,
                              Clock clock) {
        this.customTaskService = customTaskService;
        this.thesaurus = thesaurus;
        this.conflictFactory = conflictFactory;
        this.customTaskInfoFactory = customTaskInfoFactory;
        this.clock = clock;
        this.propertyValueInfoService = propertyValueInfoService;
        this.customTaskHistoryInfoFactory = customTaskHistoryInfoFactory;
        this.taskService = taskService;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_TASK_OVERVIEW})
    public CustomTaskInfo getCustomTask(@PathParam("id") long id, @HeaderParam(X_CONNEXO_APPLICATION_NAME) String appCode) {
        return customTaskInfoFactory.asInfo(findTaskOrThrowException(id, appCode));
    }


    @POST
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_TASK_OVERVIEW})
    @Transactional
    public Response addCustomTask(CustomTaskInfo info, @HeaderParam(X_CONNEXO_APPLICATION_NAME) String appCode) {
        CustomTaskBuilder builder = customTaskService.newBuilder()
                .setName(info.name)
                .setLogLevel(info.logLevel)
                .setApplication(getApplicationNameFromCode(appCode))
                .setScheduleExpression(getScheduleExpression(info))
                .setTaskType(info.type)
                .setNextRecurrentTasks(this.findRecurrentTaskOrThrowException(info.nextRecurrentTasks))
                .setNextExecution(info.nextRun);

        info.properties.stream()
                .map(customTaskPropertiesInfo -> customTaskPropertiesInfo.properties)
                .flatMap(List::stream)
                .forEach(propertyInfo -> builder.addProperty(propertyInfo.key).withValue(propertyInfo.propertyValueInfo.value));

        CustomTask customTask = builder.create();
        return Response.status(Response.Status.CREATED)
                .entity(customTaskInfoFactory.asInfo(customTask))
                .build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_TASK_OVERVIEW})
    @Transactional
    public Response removeCustomTask(@PathParam("id") long id, CustomTaskInfo info) {
        String taskName = info.name;
        try {
            info.id = id;
            CustomTask task = findAndLockCustomTask(info);
            if (!task.canBeDeleted()) {
                throw new LocalizedFieldValidationException(MessageSeeds.DELETE_TASK_STATUS_BUSY, "status");
            }
            taskName = task.getName();
            task.delete();
            return Response.status(Response.Status.OK).build();
        } catch (UnderlyingSQLFailedException | CommitException ex) {
            throw new LocalizedFieldValidationException(MessageSeeds.DELETE_TASK_SQL_EXCEPTION, "status", taskName);
        }
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_TASK_OVERVIEW})
    @Transactional
    public Response updateCustomTask(@PathParam("id") long id, CustomTaskInfo info) {
        info.id = id;
        CustomTask task = findAndLockCustomTask(info);

        task.setName(info.name);
        task.setLogLevel(info.logLevel);
        task.setScheduleExpression(getScheduleExpression(info));
        task.setNextExecution(info.nextRun);
        task.setNextRecurrentTasks(this.findRecurrentTaskOrThrowException(info.nextRecurrentTasks));

        info.properties.stream()
                .map(customTaskPropertiesInfo -> customTaskPropertiesInfo.properties)
                .flatMap(List::stream)
                .forEach(propertyInfo -> task.setProperty(propertyInfo.key, propertyInfo.propertyValueInfo.value));

        task.update();
        return Response.status(Response.Status.OK).entity(customTaskInfoFactory.asInfo(task)).build();
    }

    @GET
    @Path("/types")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_TASK_OVERVIEW})
    public Response getAvailableCustomTaskTypes(@HeaderParam(X_CONNEXO_APPLICATION_NAME) String appCode, @Context SecurityContext securityContext) {
        User user = (User) securityContext.getUserPrincipal();
        List<CustomTaskTypeInfo> types = customTaskService.getAvailableCustomTasks(appCode).stream()
                .map(customTaskFactory -> {
                    List<CustomTaskPropertiesInfo> customTaskPropertiesInfos = customTaskFactory.getProperties().stream().map(propertiesInfo1 -> {
                        return new CustomTaskPropertiesInfo(propertiesInfo1.getName(),
                                propertiesInfo1.getDisplayName(),
                                propertyValueInfoService.getPropertyInfos(propertiesInfo1.getProperties()));
                    }).collect(Collectors.toList());

                    return new CustomTaskTypeInfo(customTaskFactory.getName(),
                            customTaskFactory.getDisplayName(),
                            customTaskPropertiesInfos,
                            customTaskFactory.getActionsForUser(user, appCode).stream().map(s -> s.toString()).collect(Collectors.toList()));
                })
                .collect(Collectors.toList());
        return Response.ok(types).build();
    }

    @GET
    @Path("/recurrenttask/{recurrenttaskId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_TASK_OVERVIEW})
    public CustomTaskInfo getCustomTaskByRecurrentTaskId(@PathParam("recurrenttaskId") long id, @HeaderParam(X_CONNEXO_APPLICATION_NAME) String appCode) {
        return customTaskInfoFactory.asInfo(findTaskByRecurrentTaskIdOrThrowException(id, appCode));
    }

    @PUT
    @Path("/{id}/trigger")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_TASK_OVERVIEW})
    @Transactional
    public Response triggerCustomTask(@PathParam("id") long id, CustomTaskInfo info) {
        info.id = id;
        CustomTask customTask = customTaskService.findAndLockCustomTask(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> customTaskService.findCustomTask(info.id)
                                .map(CustomTask::getVersion)
                                .orElse(null))
                        .withMessageTitle(MessageSeeds.RUN_TASK_CONCURRENT_TITLE, info.name)
                        .withMessageBody(MessageSeeds.RUN_TASK_CONCURRENT_BODY, info.name)
                        .supplier());
        customTask.triggerNow();
        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Path("/{id}/history")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_TASK_OVERVIEW})
    public PagedInfoList getCustomTaskHistories(@PathParam("id") long id, @Context SecurityContext securityContext,
                                                @BeanParam JsonQueryFilter filter, @BeanParam JsonQueryParameters queryParameters, @Context UriInfo uriInfo, @HeaderParam(X_CONNEXO_APPLICATION_NAME) String appCode) {
        CustomTask task = findTaskOrThrowException(id, appCode);
        CustomTaskOccurrenceFinder occurrencesFinder = task.getOccurrencesFinder()
                .setStart(queryParameters.getStart().orElse(0))
                .setLimit(queryParameters.getLimit().orElse(0) + 1);

        if (filter.hasProperty("startedOnFrom")) {
            if (filter.hasProperty("startedOnTo")) {
                occurrencesFinder.withStartDateIn(filter.getClosedRange("startedOnFrom", "startedOnTo"));
            } else {
                occurrencesFinder.withStartDateIn(Range.greaterThan(filter.getInstant("startedOnFrom")));
            }
        } else if (filter.hasProperty("startedOnTo")) {
            occurrencesFinder.withStartDateIn(Range.closed(Instant.EPOCH, filter.getInstant("startedOnTo")));
        }
        if (filter.hasProperty("finishedOnFrom")) {
            if (filter.hasProperty("finishedOnTo")) {
                occurrencesFinder.withEndDateIn(filter.getClosedRange("finishedOnFrom", "finishedOnTo"));
            } else {
                occurrencesFinder.withEndDateIn(Range.greaterThan(filter.getInstant("finishedOnFrom")));
            }
        } else if (filter.hasProperty("finishedOnTo")) {
            occurrencesFinder.withEndDateIn(Range.closed(Instant.EPOCH, filter.getInstant("finishedOnTo")));
        }

        /*if (filter.hasProperty("startedOnFrom")) {
            occurrencesFinder.withStartDateIn(Range.closed(filter.getInstant("startedOnFrom"),
                    filter.hasProperty("startedOnTo") ? filter.getInstant("startedOnTo") : Instant.now()));
        } else if (filter.hasProperty("startedOnTo")) {
            occurrencesFinder.withStartDateIn(Range.closed(Instant.EPOCH, filter.getInstant("startedOnTo")));
        }
        if (filter.hasProperty("finishedOnFrom")) {
            occurrencesFinder.withEndDateIn(Range.closed(filter.getInstant("finishedOnFrom"),
                    filter.hasProperty("finishedOnTo") ? filter.getInstant("finishedOnTo") : Instant.now()));
        } else if (filter.hasProperty("finishedOnTo")) {
            occurrencesFinder.withStartDateIn(Range.closed(Instant.EPOCH, filter.getInstant("finishedOnTo")));
        }*/

        if (filter.hasProperty("status")) {
            occurrencesFinder.withStatus(filter.getStringList("status")
                    .stream()
                    .map(CustomTaskStatus::valueOf)
                    .collect(Collectors.toList()));
        }

        History<CustomTask> history = task.getHistory();
        List<CustomTaskHistoryInfo> infos = occurrencesFinder.stream()
                .map(occurrence -> customTaskHistoryInfoFactory.asInfo(history, occurrence))
                .collect(Collectors.toList());

        return PagedInfoList.fromPagedList("data", infos, queryParameters);
    }

    @GET
    @Path("/{id}/history/{occurrenceId}/log")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_TASK_OVERVIEW})
    public CustomTaskOccurrenceLogInfos getCustomTaskHistoryLog(@PathParam("id") long id, @PathParam("occurrenceId") long occurrenceId,
                                                                @Context SecurityContext securityContext, @Context UriInfo uriInfo, @HeaderParam(X_CONNEXO_APPLICATION_NAME) String appCode) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        CustomTask task = findTaskOrThrowException(id, appCode);
        CustomTaskOccurrence occurrence = fetchCustomTaskOccurrence(occurrenceId, task);
        LogEntryFinder finder = occurrence.getLogsFinder()
                .setStart(queryParameters.getStartInt())
                .setLimit(queryParameters.getLimit());

        List<? extends LogEntry> occurrences = finder.find();

        CustomTaskOccurrenceLogInfos infos = new CustomTaskOccurrenceLogInfos(queryParameters.clipToLimit(occurrences), thesaurus);
        infos.total = queryParameters.determineTotal(occurrences.size());
        return infos;
    }

    @GET
    @Path("/history/{occurrenceId}/logs")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_TASK_OVERVIEW})
    public PagedInfoList getCustomTaskLogByOccurrence(@PathParam("occurrenceId") long occurrenceId, @BeanParam JsonQueryParameters queryParameters) {
        LogEntryFinder finder = findCustomTaskOccurrenceOrThrowException(occurrenceId).getLogsFinder();
        queryParameters.getStart().ifPresent(finder::setStart);
        queryParameters.getLimit().ifPresent(finder::setLimit);
        List<CustomTaskOccurrenceLogInfo> infos = finder.find()
                .stream()
                .map(CustomTaskOccurrenceLogInfo::from)
                .collect(Collectors.toList());

        return PagedInfoList.fromPagedList("data", infos, queryParameters);
    }

    @GET
    @Path("/history/{occurrenceId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_TASK_OVERVIEW})
    public CustomTaskHistoryInfo getCustomTaskOccurrence(@PathParam("occurrenceId") long occurrenceId, @BeanParam JsonQueryParameters queryParameters) {
        return customTaskHistoryInfoFactory.asInfo(findCustomTaskOccurrenceOrThrowException(occurrenceId));
    }

    private ScheduleExpression getScheduleExpression(CustomTaskInfo info) {
        return info.schedule == null ? Never.NEVER : info.schedule.toExpression();
    }

    private CustomTask findAndLockCustomTask(CustomTaskInfo info) {
        return customTaskService.findAndLockCustomTask(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> customTaskService.findCustomTask(info.id)
                                .map(CustomTask::getVersion)
                                .orElse(null))
                        .supplier());
    }

    private CustomTask findTaskByRecurrentTaskIdOrThrowException(long id, String appCode) {
        String application = getApplicationNameFromCode(appCode);
        return customTaskService.findCustomTaskByRecurrentTask(id)
                .filter(customTask -> application.equals(customTask.getApplication()))
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    private CustomTask findTaskOrThrowException(long id, String appCode) {
        String application = getApplicationNameFromCode(appCode);
        return customTaskService.findCustomTask(id)
                .filter(customTask -> application.equals(customTask.getApplication()))
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    private List<RecurrentTask> findRecurrentTaskOrThrowException(List<TaskInfo> nextRecurrentTasks) {
        List<RecurrentTask> recurrentTasks = new ArrayList<>();
        if (nextRecurrentTasks != null) {
            nextRecurrentTasks.forEach(taskInfo -> {
                recurrentTasks.add(taskService.getRecurrentTask(taskInfo.id)
                        .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND)));

            });
        }
        return recurrentTasks;
    }

    private CustomTaskOccurrence findCustomTaskOccurrenceOrThrowException(long occurrenceId) {
        return customTaskService.findCustomTaskOccurrence(occurrenceId)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    private CustomTaskOccurrence fetchCustomTaskOccurrence(long id, CustomTask task) {
        return task.getOccurrence(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    private String getApplicationNameFromCode(String appCode) {
        switch (appCode) {
            case "MDC":
                return "MultiSense";
            case "INS":
                return "Insight";
            default:
                return appCode;
        }
    }
}
