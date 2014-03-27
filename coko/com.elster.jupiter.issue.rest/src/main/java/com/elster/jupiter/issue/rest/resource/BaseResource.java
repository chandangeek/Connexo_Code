package com.elster.jupiter.issue.rest.resource;

import com.elster.jupiter.issue.share.service.IssueHelpService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseResource {
    private RestQueryService queryService;
    private TransactionService transactionService;

    private IssueService issueService;
    private IssueHelpService issueHelpService; // TODO remove parameter when events will be defined by MDC
    private UserService userService;

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

    @Inject
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
    protected UserService getUserService() {
        return userService;
    }

    protected List<Long> parseLongParams(List<String> list) {
        List<Long> resultList = new ArrayList<>();
        if (list != null) {
            for (String param : list){
                try {
                    resultList.add(Long.parseLong(param));
                } catch (NumberFormatException ex) {
                    throw new WebApplicationException(Response.Status.BAD_REQUEST);
                }
            }
        }
        return resultList;
    }
}
