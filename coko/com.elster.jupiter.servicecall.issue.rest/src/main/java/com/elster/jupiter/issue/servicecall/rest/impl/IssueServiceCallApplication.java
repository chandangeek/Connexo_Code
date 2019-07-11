/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.servicecall.rest.impl;

import com.elster.jupiter.issue.rest.resource.IssueResourceHelper;
import com.elster.jupiter.issue.rest.response.IssueActionInfoFactory;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.license.License;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.servicecall.issue.ServiceCallIssueService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;

import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component(name = "com.elster.jupiter.issue.servicecall.rest", service = {Application.class, MessageSeedProvider.class, TranslationKeyProvider.class}, immediate = true, property = {"alias=/itk", "app=" + IssueServiceCallApplication.APP_KEY, "name=" + IssueServiceCallApplication.ISSUE_SERVICE_CALL_REST_COMPONENT})
public class IssueServiceCallApplication extends Application {
    public static final String APP_KEY = "SYS";
    public static final String ISSUE_SERVICE_CALL_REST_COMPONENT = "SIR";

    private volatile TransactionService transactionService;
    private volatile UserService userService;
    private volatile IssueService issueService;
    private volatile ServiceCallIssueService serviceCallIssueService;
    private volatile IssueActionService issueActionService;
    private volatile NlsService nlsService;
    private volatile Thesaurus thesaurus;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(IssueResource.class);
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
        this.issueActionService = issueService.getIssueActionService();
    }

    @Reference
    public void setIssueServiceCallService(ServiceCallIssueService serviceCallIssueService) {
        this.serviceCallIssueService = serviceCallIssueService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        Thesaurus domainThesaurus = nlsService.getThesaurus(ServiceCallIssueService.COMPONENT_NAME, Layer.DOMAIN);
        Thesaurus restThesaurus = nlsService.getThesaurus(ISSUE_SERVICE_CALL_REST_COMPONENT, Layer.REST);
        this.thesaurus = domainThesaurus.join(restThesaurus);
    }

    @Reference(target = "(com.elster.jupiter.license.rest.key=" + APP_KEY + ")")
    public void setLicense(License license) {
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> hashSet = new HashSet<>();
        hashSet.addAll(super.getSingletons());
        hashSet.add(new HK2Binder());
        return Collections.unmodifiableSet(hashSet);
    }

    class HK2Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(issueService).to(IssueService.class);
            bind(issueActionService).to(IssueActionService.class);
            bind(serviceCallIssueService).to(ServiceCallIssueService.class);
            bind(userService).to(UserService.class);
            bind(transactionService).to(TransactionService.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(nlsService).to(NlsService.class);
            bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
            bind(ServiceCallIssueInfoFactory.class).to(ServiceCallIssueInfoFactory.class);
            bind(IssueResourceHelper.class).to(IssueResourceHelper.class);
            bind(IssueActionInfoFactory.class).to(IssueActionInfoFactory.class);
        }
    }
}
