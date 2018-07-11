/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.insight.issue.datavalidation.rest.impl;

import com.elster.jupiter.issue.rest.resource.IssueResourceHelper;
import com.elster.jupiter.issue.rest.response.IssueActionInfoFactory;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.license.License;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;


import com.elster.insight.issue.datavalidation.IssueDataValidationService;

import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component(name = "com.energyict.mdm.issue.datavalidation.rest", service = {Application.class}, immediate = true, property = {"alias=/iuv", "app=MDM", "name=" + IssueDataValidationApplication.ISSUEDATAVALIDATION_REST_COMPONENT})
public class IssueDataValidationApplication extends Application {
    private static final String APP_KEY = "INS";
    static final String ISSUEDATAVALIDATION_REST_COMPONENT = "IUV";

    private volatile TransactionService transactionService;
    private volatile UserService userService;
    private volatile IssueService issueService;
    private volatile IssueDataValidationService issueDataValidationService;
    private volatile IssueActionService issueActionService;
    private volatile MeteringService meteringService;
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
    public void setIssueDataValidationService(IssueDataValidationService issueDataValidationService) {
        this.issueDataValidationService = issueDataValidationService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(IssueDataValidationService.COMPONENT_NAME, Layer.DOMAIN)
                .join(nlsService.getThesaurus(MeteringService.COMPONENTNAME, Layer.DOMAIN));
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
            bind(issueDataValidationService).to(IssueDataValidationService.class);
            bind(userService).to(UserService.class);
            bind(transactionService).to(TransactionService.class);
            bind(meteringService).to(MeteringService.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(nlsService).to(NlsService.class);
            bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
            bind(ReadingTypeInfoFactory.class).to(ReadingTypeInfoFactory.class);
            bind(DataValidationIssueInfoFactory.class).to(DataValidationIssueInfoFactory.class);
            bind(IssueResourceHelper.class).to(IssueResourceHelper.class);
            bind(IssueActionInfoFactory.class).to(IssueActionInfoFactory.class);
        }
    }
}
