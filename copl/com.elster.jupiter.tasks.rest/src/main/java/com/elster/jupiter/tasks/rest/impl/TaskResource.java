package com.elster.jupiter.tasks.rest.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.RecurrentTaskFilterSpecification;
import com.elster.jupiter.tasks.TaskFinder;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

    @Inject
    public TaskResource(TaskService taskService, RestQueryService queryService, Thesaurus thesaurus, TransactionService transactionService,TimeService timeService) {
        this.queryService = queryService;
        this.thesaurus = thesaurus;
        this.transactionService = transactionService;
        this.taskService = taskService;
        this.timeService = timeService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public TaskInfos getDataExportTasks(@Context UriInfo uriInfo) {
        QueryParameters params = QueryParameters.wrap(uriInfo.getQueryParameters());

        RecurrentTaskFilterSpecification filterSpec = new RecurrentTaskFilterSpecification();
        if (params.get("filter") != null) {
            JsonQueryFilter filter = new JsonQueryFilter(params.get("filter").get(0));
            filterSpec.applications.addAll(filter.getStringList("application"));
            filterSpec.queues.addAll(filter.getStringList("queue"));
        }
        TaskFinder finder = taskService.getTaskFinder(filterSpec)
                .setStart(params.getStartInt())
                .setLimit(params.getLimit());

        List<? extends RecurrentTask> list = finder.find();
        TaskInfos infos = new TaskInfos(params.clipToLimit(list), thesaurus, timeService);
        return infos;
    }

    private List<? extends RecurrentTask> queryTasks(QueryParameters queryParameters) {
        Query<? extends RecurrentTask> query = taskService.getTaskQuery();
        RestQuery<? extends RecurrentTask> restQuery = queryService.wrap(query);
        return restQuery.select(queryParameters);
    }

    @GET
    @Path("/applications")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public List<ApplicationInfo> getApplications(@Context UriInfo uriInfo) {
        List<ApplicationInfo> result = new ArrayList<ApplicationInfo>();
        result.add(new ApplicationInfo("MultiSense"));
        //result.add(new ApplicationInfo("Insight"));
        return result;
    }

    @GET
    @Path("/queues")
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
