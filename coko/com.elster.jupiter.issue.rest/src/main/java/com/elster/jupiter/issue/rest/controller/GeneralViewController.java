package com.elster.jupiter.issue.rest.controller;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.Issue;
import com.elster.jupiter.issue.IssueService;
import com.elster.jupiter.issue.rest.response.*;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.conditions.Condition;
import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.elster.jupiter.util.conditions.Where.where;

@Path("/issue")
public class GeneralViewController {
    private final RestQueryService queryService;
    private final IssueService issueService;
    private final TransactionService transactionService;


    @Inject
    public GeneralViewController(RestQueryService queryService, IssueService issueService, TransactionService transactionService) {
        this.queryService = queryService;
        this.issueService = issueService;
        this.transactionService = transactionService;
    }

     @GET
     @Produces(MediaType.APPLICATION_JSON)
     public IssueList getAllIssues(@Context UriInfo uriInfo) {
        QueryParameters params = QueryParameters.wrap(uriInfo.getQueryParameters());
        IssueList resultList = queryIssueList(true, params);
        return resultList;
    }

    @GET
    @Path("/groupedlist")
    @Produces(MediaType.APPLICATION_JSON)
    public Object getGroupedList(@Context UriInfo uriInfo) {
        QueryParameters params = QueryParameters.wrap(uriInfo.getQueryParameters());
        Map<String, Long> resultMap = Collections.EMPTY_MAP;
        if (params.get("reason") != null) {
            try (TransactionContext context = transactionService.getContext()) {
                resultMap = issueService.getIssueGroupList(params.get("reason").get(0), false, params.getStart(), params.getLimit());
                context.commit();
            }
        }
        IssueGroupList resultList = new IssueGroupList();
        for(Map.Entry<String, Long> groupEntry : resultMap.entrySet()) {
            IssueGroupListRow rIssue = new IssueGroupListRow();
            rIssue.setReason(groupEntry.getKey());
            rIssue.setNumber(groupEntry.getValue());
            resultList.add(rIssue);
        }
    return resultList;
    }

    private IssueList queryIssueList(boolean maySeeAny, QueryParameters queryParameters) {
        Query<Issue> query = issueService.getIssueListQuery();
        query.setEager();
        Condition condition = null;
        if (queryParameters.get("reason") != null) {
            condition = where("reason").isEqualTo(queryParameters.get("reason").get(0));
        }
        List<String> sortList = queryParameters.get("sort");
        //condition = condition.and(where("serviceLocation.mainAddress.townDetail.country").isEqualTo("BE"));
        List<Issue> list = new LinkedList<Issue>();
        if (sortList != null && condition != null) {
            list = query.select(condition, queryParameters.getStart(), queryParameters.getLimit(), sortList.get(0));

        } else if (condition != null) {
            list = query.select(condition, queryParameters.getStart(), queryParameters.getLimit());
        } else if (sortList != null) {
            RestQuery<Issue> restQuery = queryService.wrap(query);
            list = restQuery.select(queryParameters, sortList.get(0));
        } else {
            RestQuery<Issue> restQuery = queryService.wrap(query);

            list = restQuery.select(queryParameters);
        }
        IssueList resultList = new IssueList();
        for (Issue issue : list) {
            IssueListRow rowIssue = new IssueListRow(issue);
            resultList.add(rowIssue);
        }
        return resultList;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public IssuePreview getIssueById(@PathParam("id") long id) {
        Optional<Issue> issue = issueService.getIssueById(id);
        if (!issue.isPresent()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return new IssuePreview(issue.get());
    }
}
