/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.RecurrentTaskFilterSpecification;
import com.elster.jupiter.tasks.TaskFinder;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.tasks.security.Privileges;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.security.Principal;
import java.time.Clock;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Created by igh on 27/10/2015.
 */
@Path("/task")
public class TaskResource {

    private final RestQueryService queryService;
    private final Thesaurus thesaurus;
    private final TransactionService transactionService;
    private final TaskService taskService;
    private final TimeService timeService;
    private final Clock clock;

    @Inject
    public TaskResource(TaskService taskService, RestQueryService queryService, Thesaurus thesaurus, TransactionService transactionService, TimeService timeService, Clock clock) {
        this.queryService = queryService;
        this.thesaurus = thesaurus;
        this.transactionService = transactionService;
        this.taskService = taskService;
        this.timeService = timeService;
        this.clock = clock;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_TASK_OVERVIEW})
    public TaskInfos getDataExportTasks(@Context UriInfo uriInfo, @Context SecurityContext securityContext) {
        QueryParameters params = QueryParameters.wrap(uriInfo.getQueryParameters());

        RecurrentTaskFilterSpecification filterSpec = new RecurrentTaskFilterSpecification();
        if (params.get("filter") != null) {
            JsonQueryFilter filter = new JsonQueryFilter(params.get("filter").get(0));
            filterSpec.applications.addAll(filter.getStringList("application"));
            filterSpec.queues.addAll(filter.getStringList("queue"));
            filterSpec.startedOnFrom = filter.getInstant("startedOnFrom");
            filterSpec.startedOnTo = filter.getInstant("startedOnTo");
        }
        TaskFinder finder = taskService.getTaskFinder(filterSpec, params.getStartInt(), params.getLimit() + 1);

        List<? extends RecurrentTask> list = finder.find();
        Principal principal = (Principal) securityContext.getUserPrincipal();
        Locale locale = Locale.getDefault();
        if (principal instanceof User) {
            User user = (User) principal;
            if (user.getLocale().isPresent()) {
                locale = user.getLocale().get();
            }
        }
        TaskInfos infos = new TaskInfos(params.clipToLimit(list), thesaurus, timeService, locale, clock);
        infos.total = params.determineTotal(list.size());
        return infos;
    }

    @GET
    @Path("/applications")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_TASK_OVERVIEW})
    public List<IdWithNameInfo> getApplications(@Context UriInfo uriInfo) {
        List<RecurrentTask> tasks = taskService.getRecurrentTasks();
        List<String> applicationNames = new ArrayList<String>();
        for (RecurrentTask task : tasks)  {
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
    @RolesAllowed({Privileges.Constants.VIEW_TASK_OVERVIEW})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public List<QueueInfo> getQueues(@Context UriInfo uriInfo) {
        List<RecurrentTask> tasks = taskService.getRecurrentTasks();
        List<String> queueNames = new ArrayList<String>();
        for (RecurrentTask task : tasks)  {
            queueNames.add(task.getDestination().getName());
        }
        Set<String> set = new HashSet<>();
        set.addAll(queueNames);
        List<QueueInfo> queues = new ArrayList<QueueInfo>();
        for (String queueName : set) {
            queues.add(new QueueInfo(queueName));
        }
        return queues;
    }


}
