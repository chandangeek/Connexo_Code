package com.elster.jupiter.issue.rest.controller;

import com.elster.jupiter.issue.IssueService;
import com.elster.jupiter.issue.rest.request.AssignIssueRequest;
import com.elster.jupiter.issue.rest.request.CloseIssueRequest;
import com.elster.jupiter.issue.rest.response.ActionRequestFail;
import com.elster.jupiter.issue.rest.response.BaseActionResponse;
import com.elster.jupiter.issue.rest.response.IssueShortInfo;
import com.elster.jupiter.issue.rest.transactions.AssignIssueTransaction;
import com.elster.jupiter.issue.rest.transactions.CloseIssuesTransaction;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;

@Path("/issue")
public class IssueActionController {
    private final RestQueryService queryService;
    private final IssueService issueService;
    private final TransactionService transactionService;


    @Inject
    public IssueActionController(RestQueryService queryService, IssueService issueService, TransactionService transactionService) {
        this.queryService = queryService;
        this.issueService = issueService;
        this.transactionService = transactionService;
    }

    @PUT
    @Path("/close")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public BaseActionResponse closeIssues(CloseIssueRequest request){
        return transactionService.execute(new CloseIssuesTransaction(request, issueService));
    }

    @PUT
    @Path("/assign")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public BaseActionResponse assignIssues(AssignIssueRequest request){
        return transactionService.execute(new AssignIssueTransaction(request, issueService));
    }
}
