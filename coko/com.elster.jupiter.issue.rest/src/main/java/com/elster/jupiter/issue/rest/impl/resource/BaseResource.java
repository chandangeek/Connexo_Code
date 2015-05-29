package com.elster.jupiter.issue.rest.impl.resource;

import com.elster.jupiter.issue.rest.resource.StandardParametersBean;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public abstract class BaseResource {
    private RestQueryService queryService;
    private TransactionService transactionService;

    private IssueService issueService;
    private IssueCreationService issueCreationService;
    private IssueActionService issueActionService;
    private UserService userService;
    private MeteringService meteringService;

    private Thesaurus thesaurus;

    public BaseResource() {
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

    @Inject
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    protected MeteringService getMeteringService() {
        return meteringService;
    }

    @Inject
    public void setIssueCreationService(IssueCreationService issueCreationService) {
        this.issueCreationService = issueCreationService;
    }

    protected IssueCreationService getIssueCreationService() {
        return issueCreationService;
    }

    @Inject
    public void setIssueActionService(IssueActionService issueActionService) {
        this.issueActionService = issueActionService;
    }

    protected IssueActionService getIssueActionService() {
        return issueActionService;
    }

    @Inject
    public void setThesaurus(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    protected Thesaurus getThesaurus() {
        return thesaurus;
    }

    protected void validateMandatory(StandardParametersBean params, String... mandatoryParameters) {
        if (mandatoryParameters != null) {
            for (String mandatoryParameter : mandatoryParameters) {
                String value = params.getFirst(mandatoryParameter);
                if (value == null) {
                    throw new WebApplicationException(Response.Status.BAD_REQUEST);
                }
            }
        }
    }
}
