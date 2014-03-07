package com.elster.jupiter.issue.rest.resource;

import com.elster.jupiter.issue.share.service.IssueHelpService;
import com.elster.jupiter.issue.share.service.IssueMainService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;

import javax.inject.Inject;

public abstract class BaseResource {
    private RestQueryService queryService;
    private TransactionService transactionService;

    private IssueService issueService;
    private IssueMainService issueMainService;
    private IssueHelpService issueHelpService; // TODO remove parameter when events will be defined by MDC

    public BaseResource(){
    }

    protected RestQueryService getQueryService() {
        return queryService;
    }

    @Inject
    public void setQueryService(RestQueryService queryService) {
        this.queryService = queryService;
    }

    @Inject
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }
    protected IssueService getIssueService() {
        return issueService;
    }
    @Inject
    public void setIssueMainService(IssueMainService issueMainService) {
        this.issueMainService = issueMainService;
    }
    protected IssueMainService getIssueMainService() {
        return issueMainService;
    }
    @Inject
    public void setIssueHelpService(IssueHelpService issueHelpService) {
        this.issueHelpService = issueHelpService;
    }
    protected IssueHelpService getIssueHelpService() {
        return issueHelpService;
    }
    @Inject
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    protected TransactionService getTransactionService() {
        return transactionService;
    }
}
