package com.elster.jupiter.issue.rest.controller;

import com.elster.jupiter.issue.IssueService;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;

import javax.inject.Inject;

public abstract class BaseController {
    private RestQueryService queryService;
    private IssueService issueService;
    private TransactionService transactionService;

    public BaseController(){
    }

    protected RestQueryService getQueryService() {
        return queryService;
    }

    @Inject
    public void setQueryService(RestQueryService queryService) {
        this.queryService = queryService;
    }

    protected IssueService getIssueService() {
        return issueService;
    }

    @Inject
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    protected TransactionService getTransactionService() {
        return transactionService;
    }

    @Inject
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
}
