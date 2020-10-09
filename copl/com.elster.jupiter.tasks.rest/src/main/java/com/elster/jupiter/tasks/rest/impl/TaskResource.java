/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.RecurrentTaskFilterSpecification;
import com.elster.jupiter.tasks.TaskFinder;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.tasks.security.Privileges;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.Pair;

import com.fasterxml.jackson.databind.JsonNode;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.security.Principal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by igh on 27/10/2015.
 */
@Path("/task")
public class TaskResource {

    private static final String ADD_CERTIFICATE_REQUEST_DATA_TASK_NAME = "Add Certificate Request Data Task";
    private final Thesaurus thesaurus;
    private final TaskService taskService;
    private final TimeService timeService;
    private final Clock clock;

    @Inject
    public TaskResource(TaskService taskService, Thesaurus thesaurus, TimeService timeService, Clock clock) {
        this.thesaurus = thesaurus;
        this.taskService = taskService;
        this.timeService = timeService;
        this.clock = clock;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_TASK_OVERVIEW, Privileges.Constants.ADMINISTER_TASK_OVERVIEW})
    public PagedInfoList getTasks(@BeanParam JsonQueryParameters queryParams, @BeanParam JsonQueryFilter filter, @Context SecurityContext securityContext) {
        if (!queryParams.getStart().isPresent() || !queryParams.getLimit().isPresent()) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        TaskFinder finder = taskService.getTaskFinder(filterSpec(filter, queryParams), queryParams.getStart().get(), queryParams.getLimit().get() + 1);

        List<? extends RecurrentTask> list = finder.find();
        Principal principal = (Principal) securityContext.getUserPrincipal();
        Locale locale = determineLocale(principal);

        List<TaskInfo> taskInfos = list.stream().map(t -> new TaskInfo(t, thesaurus, timeService, locale, clock)).collect(Collectors.toList());
        return PagedInfoList.fromPagedList("tasks", taskInfos, queryParams);
    }

    @GET
    @Path("/count")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_TASK_OVERVIEW, Privileges.Constants.ADMINISTER_TASK_OVERVIEW})
    public Response getTasksCount(@BeanParam JsonQueryFilter filter, @Context SecurityContext securityContext) {
        TaskFinder finder = taskService.getTaskFinderWithoutPagination(filterSpec(filter, null));
        return Response.ok(finder.count()).build();
    }

    private RecurrentTaskFilterSpecification filterSpec(JsonQueryFilter filter, JsonQueryParameters queryParams) {
        RecurrentTaskFilterSpecification filterSpec = new RecurrentTaskFilterSpecification();
        if (filter != null) {
            filterSpec.applications.addAll(filter.getStringList("application"));
            filterSpec.queues.addAll(filter.getStringList("queue"));
            filterSpec.queueTypes.addAll(filter.getStringList("queueType"));
            filterSpec.startedOnFrom = filter.getInstant("startedOnFrom");
            filterSpec.startedOnTo = filter.getInstant("startedOnTo");
            filterSpec.suspended.addAll(filter.getStringList("suspended"));
            filterSpec.nextExecutionFrom = filter.getInstant("nextRunFrom");
            filterSpec.nextExecutionTo = filter.getInstant("nextRunTo");
            if (filter.hasProperty("priority")) {
                applyFilterWithOperator(filter, "priority", filterSpec.priority);
                validateNumberBetweenFilterOrThrowException(filterSpec.priority);
            }
            if (queryParams != null) {
                filterSpec.sortingColumns = queryParams.getSortingColumns();
            }
        }
        return filterSpec;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_TASK_OVERVIEW, Privileges.Constants.ADMINISTER_TASK_OVERVIEW})
    public TaskInfo getTask(@PathParam("id") long id, @Context UriInfo uriInfo, @Context SecurityContext securityContext) {
        Principal principal = (Principal) securityContext.getUserPrincipal();
        Locale locale = determineLocale(principal);
        RecurrentTask recurrentTask = taskService.getRecurrentTask(id)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));

        return new TaskInfo(recurrentTask, thesaurus, timeService, locale, clock);
    }


    @GET
    @Path("/applications")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_TASK_OVERVIEW, Privileges.Constants.ADMINISTER_TASK_OVERVIEW})
    public List<IdWithNameInfo> getApplications(@Context UriInfo uriInfo) {
        List<RecurrentTask> tasks = taskService.getRecurrentTasks();
        List<String> applicationNames = new ArrayList<String>();
        for (RecurrentTask task : tasks) {
            applicationNames.add(task.getApplication());
        }
        Set<String> set = new HashSet<>();
        set.addAll(applicationNames);
        List<IdWithNameInfo> applications = new ArrayList<>();
        for (String applicationName : set) {
            applications.add(new IdWithNameInfo(applicationName, thesaurus.getString(applicationName, applicationName)));
        }
        return applications;
    }

    @GET
    @Path("/queues")
    @RolesAllowed({Privileges.Constants.VIEW_TASK_OVERVIEW, Privileges.Constants.ADMINISTER_TASK_OVERVIEW})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public List<QueueInfo> getQueues(@Context UriInfo uriInfo) {
        List<RecurrentTask> tasks = taskService.getRecurrentTasks();
        List<String> applicationNames = uriInfo.getQueryParameters().get("application");
        List<String> queueNames = new ArrayList<String>();
        for (RecurrentTask task : tasks) {
            if (((applicationNames == null) || (applicationNames.size() == 0)) ||
                    applicationNames.contains(task.getApplication())) {
                queueNames.add(task.getDestination().getName());
            }
        }
        Set<String> set = new HashSet<>();
        set.addAll(queueNames);
        List<QueueInfo> queues = new ArrayList<QueueInfo>();
        for (String queueName : set) {
            queues.add(new QueueInfo(queueName));
        }
        return queues;
    }

    @GET
    @Path("/queueTypes")
    @RolesAllowed({Privileges.Constants.VIEW_TASK_OVERVIEW, Privileges.Constants.ADMINISTER_TASK_OVERVIEW})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public List<QueueTypeInfo> getQueueTypes(@Context UriInfo uriInfo) {
        List<RecurrentTask> tasks = taskService.getRecurrentTasks();
        List<String> applicationNames = uriInfo.getQueryParameters().get("application");
        Set<String> queueTypeNamesSet = new HashSet<>();
        for (RecurrentTask task : tasks) {
            if (((applicationNames == null) || (applicationNames.size() == 0)) ||
                    applicationNames.contains(task.getApplication())) {
                queueTypeNamesSet.add(task.getDestination().getQueueTypeName());
            }
        }
        return queueTypeNamesSet.stream().map(QueueTypeInfo::new).collect(Collectors.toList());
    }

    @GET
    @Path("/byapplication")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_TASK_OVERVIEW, Privileges.Constants.ADMINISTER_TASK_OVERVIEW})
    public List<TaskMinInfo> getTasksByApplication(@Context UriInfo uriInfo) {
        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
        String applicationName = queryParameters.containsKey("application") ? queryParameters.getFirst("application") : "";
        List<TaskMinInfo> tasks = taskService.getRecurrentTasks()
                .stream()
                .filter(recurrentTask -> recurrentTask.getApplication().compareToIgnoreCase(applicationName) == 0)
                .map(rt -> TaskMinInfo.from(thesaurus, rt))
                .collect(Collectors.toList());
        return tasks;
    }

    @GET
    @Path("/triggers/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_TASK_OVERVIEW, Privileges.Constants.ADMINISTER_TASK_OVERVIEW})
    public TaskTrigger getTriggers(@PathParam("id") long id) {
        RecurrentTask recurrentTask = taskService.getRecurrentTask(id)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));

        return TaskTrigger.from(thesaurus, recurrentTask);
    }

    @GET
    @Path("/compatiblequeues/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_TASK_OVERVIEW, Privileges.Constants.ADMINISTER_TASK_OVERVIEW})
    public Response getCompatibleQueues(@PathParam("id") long id) {
        RecurrentTask recurrentTask = taskService.getRecurrentTask(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        List<QueueInfo> queues = taskService.getCompatibleQueues4(recurrentTask.getDestination().getName())
                .stream()
                .map(destinationSpec -> new QueueInfo(destinationSpec.getName()))
                .collect(Collectors.toList());

        return Response.status(Response.Status.OK).entity(queues).build();
    }

    @PUT
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.ADMINISTER_TASK_OVERVIEW})
    public Response modifyTask(TaskMinInfo info) {
        RecurrentTask recurrentTask = taskService.getRecurrentTask(info.id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        if (!Checks.is(info.queue).empty()) {
            recurrentTask.setDestination(info.queue);
        }
        if (info.priority != null) {
            recurrentTask.setPriority(info.priority);
        }
        if (!Checks.is(info.queue).empty() || info.priority != null) {
            recurrentTask.save();
        }
        return Response.status(Response.Status.OK).build();
    }


    @POST
    @Path("/tasks/{id}/suspend/{suspendTime}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed({Privileges.Constants.SUSPEND_TASK_OVERVIEW})
    public TaskInfo suspendGeneralTask(@PathParam("id") long id, @PathParam("suspendTime") long suspendTime, @Context SecurityContext securityContext) {

        Principal principal = (Principal) securityContext.getUserPrincipal();
        Locale locale = determineLocale(principal);

        Instant instant = Instant.ofEpochMilli(suspendTime);

        RecurrentTask recurrentTask = taskService.getRecurrentTask(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        recurrentTask.setSuspendUntil(instant);
        TaskInfo taskInfo = new TaskInfo(recurrentTask, thesaurus, timeService, locale, clock);
        return taskInfo;

    }

    private Locale determineLocale(Principal principal) {
        Locale locale = Locale.getDefault();
        if (principal instanceof User) {
            User user = (User) principal;
            if (user.getLocale().isPresent()) {
                locale = user.getLocale().get();
            }
        }
        return locale;
    }

    private void applyFilterWithOperator(JsonQueryFilter filter, String propertyName, RecurrentTaskFilterSpecification.NumberBetweenFilter specField) {
        Pair<RecurrentTaskFilterSpecification.Operator, JsonNode> operatorWithValues = filter.getProperty(propertyName, this::parseRelationalOperator);
        RecurrentTaskFilterSpecification.Operator operator = operatorWithValues.getFirst();
        JsonNode values = operatorWithValues.getLast();
        operator.apply(values, specField);
    }

    private Pair<RecurrentTaskFilterSpecification.Operator, JsonNode> parseRelationalOperator(JsonNode jsonNode) {
        JsonNode operatorNode = jsonNode.get("operator");
        JsonNode criteriaNode = jsonNode.get("criteria");
        if (operatorNode == null || !operatorNode.isTextual() || criteriaNode == null || criteriaNode.isNull()) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        String operatorName = operatorNode.asText();
        RecurrentTaskFilterSpecification.Operator operator = RecurrentTaskFilterSpecification.Operator.findOperator(operatorName).orElseThrow(
                () -> new WebApplicationException(Response.Status.BAD_REQUEST));

        return Pair.of(operator, criteriaNode);
    }

    private void validateNumberBetweenFilterOrThrowException(RecurrentTaskFilterSpecification.NumberBetweenFilter numberBetweenFilter) {
        if (numberBetweenFilter.operator == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }

    @PUT
    @Transactional
    @Path("/runaddcertreqdatatask")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.EXECUTE_ADD_CERTIFICATE_REQUEST_DATA_TASK})
    public Response runAddCertificateRequestDataTask() {
        RecurrentTask recurrentTask = taskService.getRecurrentTask(ADD_CERTIFICATE_REQUEST_DATA_TASK_NAME).orElseThrow(
                () -> new WebApplicationException(Response.Status.NOT_FOUND));
        recurrentTask.triggerNow();
        return Response.ok().build();
    }
}
