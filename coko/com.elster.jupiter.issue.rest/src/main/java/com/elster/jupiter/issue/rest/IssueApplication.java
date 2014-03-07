package com.elster.jupiter.issue.rest;


import com.elster.jupiter.issue.rest.resource.HelpResource;
import com.elster.jupiter.issue.rest.resource.IssueAssignResource;
import com.elster.jupiter.issue.rest.resource.IssueResource;
import com.elster.jupiter.issue.share.service.IssueHelpService;
import com.elster.jupiter.issue.share.service.IssueMainService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.rest.util.BinderProvider;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.Set;

@Component(name = "com.elster.jupiter.issue.rest", service = Application.class, immediate = true, property = {"alias=/isu"})
public class IssueApplication extends Application implements BinderProvider {

    private volatile TransactionService transactionService;
    private volatile RestQueryService restQueryService;
    private volatile UserService userService;
    private volatile IssueService issueService;
    private volatile IssueMainService issueMainService;
    private volatile IssueHelpService issueHelpService;

    @Override
    public Binder getBinder() {
        return new AbstractBinder() {
            @Override
            protected void configure() {
                bind(issueService).to(IssueService.class);
                bind(issueMainService).to(IssueMainService.class);
                bind(issueHelpService).to(IssueHelpService.class);
                bind(userService).to(UserService.class);
                bind(transactionService).to(TransactionService.class);
                bind(restQueryService).to(RestQueryService.class);
            }
        };
    }

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.<Class<?>>of(IssueResource.class, IssueAssignResource.class, HelpResource.class);
    }

    @Activate
    public void activate() {
    }

    @Deactivate
    public void deactivate() {
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }
    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    @Reference
    public void setRestQueryService(RestQueryService restQueryService) {
        this.restQueryService = restQueryService;
    }
    @Reference
    public void setIssueMainService(IssueMainService issueMainService) {
        this.issueMainService = issueMainService;
    }
    @Reference
    public void setIssueHelpService(IssueHelpService issueHelpService) {
        this.issueHelpService = issueHelpService;
    }
}
