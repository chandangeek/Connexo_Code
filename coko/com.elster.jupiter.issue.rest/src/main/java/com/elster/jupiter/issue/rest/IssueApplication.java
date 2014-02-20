package com.elster.jupiter.issue.rest;


import com.elster.jupiter.issue.IssueService;
import com.elster.jupiter.issue.rest.controller.GeneralViewController;
import com.elster.jupiter.issue.rest.controller.IssueActionController;
import com.elster.jupiter.rest.util.BinderProvider;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.google.common.collect.ImmutableSet;
import com.google.inject.internal.util.$ImmutableSet;
import com.google.inject.internal.util.$UnmodifiableIterator;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

@Component(name = "com.elster.jupiter.issue.rest", service = Application.class, immediate = true, property = {"alias=/isu"})
public class IssueApplication extends Application implements BinderProvider {

    private volatile TransactionService transactionService;
    private volatile RestQueryService restQueryService;
    private volatile UserService userService;
    private volatile IssueService issueService;

    @Override
    public Binder getBinder() {
        return new AbstractBinder() {
            @Override
            protected void configure() {
                bind(issueService).to(IssueService.class);
                bind(userService).to(UserService.class);
                bind(transactionService).to(TransactionService.class);
                bind(restQueryService).to(RestQueryService.class);
            }
        };
    }

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.<Class<?>>of(GeneralViewController.class, IssueActionController.class);
    }

    @Activate
    public void activate(ComponentContext context) {
    }

    @Deactivate
    public void deactivate(ComponentContext context) {
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


}
