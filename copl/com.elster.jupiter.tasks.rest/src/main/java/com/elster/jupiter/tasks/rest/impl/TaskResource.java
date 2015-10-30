package com.elster.jupiter.tasks.rest.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.conditions.Order;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by igh on 27/10/2015.
 */
@Path("/task")
public class TaskResource {

    private final RestQueryService queryService;
    private final Thesaurus thesaurus;
    private final TransactionService transactionService;
    private final TaskService taskService;

    @Inject
    public TaskResource(TaskService taskService, RestQueryService queryService, Thesaurus thesaurus, TransactionService transactionService) {
        this.queryService = queryService;
        this.thesaurus = thesaurus;
        this.transactionService = transactionService;
        this.taskService = taskService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public TaskInfos getDataExportTasks(@Context UriInfo uriInfo) {
        QueryParameters params = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<? extends RecurrentTask> list = queryTasks(params);
        TaskInfos infos = new TaskInfos(params.clipToLimit(list));
        return infos;
    }

    private List<? extends RecurrentTask> queryTasks(QueryParameters queryParameters) {
        Query<? extends RecurrentTask> query = taskService.getTaskQuery();
        RestQuery<? extends RecurrentTask> restQuery = queryService.wrap(query);
        return restQuery.select(queryParameters);
    }
}
