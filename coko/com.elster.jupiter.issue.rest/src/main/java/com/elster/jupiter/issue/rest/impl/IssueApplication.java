/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.impl;

import com.elster.jupiter.issue.rest.MessageSeeds;
import com.elster.jupiter.issue.rest.TranslationKeys;
import com.elster.jupiter.issue.rest.impl.resource.*;
import com.elster.jupiter.issue.rest.resource.IssueResourceHelper;
import com.elster.jupiter.issue.rest.response.IssueActionInfoFactory;
import com.elster.jupiter.issue.rest.response.IssueInfoFactory;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleActionInfoFactory;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleExclGroupInfoFactory;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleInfoFactory;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleTemplateInfoFactory;
import com.elster.jupiter.issue.rest.response.issue.IssueInfoFactoryService;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueAssignmentService;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.LocationService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.search.location.SearchLocationService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component(name = "com.elster.jupiter.issue.rest",
           service = { Application.class, TranslationKeyProvider.class, MessageSeedProvider.class },
           immediate = true, property = { "alias=/isu", "app=SYS", "name=" + IssueApplication.ISSUE_REST_COMPONENT })
public class IssueApplication extends Application implements TranslationKeyProvider, MessageSeedProvider {

    public static final String ISSUE_REST_COMPONENT = "ISR";

    private volatile TransactionService transactionService;
    private volatile RestQueryService restQueryService;
    private volatile UserService userService;
    private volatile IssueService issueService;
    private volatile IssueCreationService issueCreationService;
    private volatile IssueActionService issueActionService;
    private volatile IssueAssignmentService issueAssignmentService;
    private volatile MeteringService meteringService;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile LocationService locationService;
    private volatile SearchLocationService searchLocationService;
    private volatile NlsService nlsService;
    private volatile Thesaurus thesaurus;
    private volatile IssueInfoFactoryService issueInfoFactoryService;
    private volatile PropertyValueInfoService propertyValueInfoService;
    private volatile Clock clock;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.<Class<?>> of(
                    AssigneeResource.class,
                    RuleResource.class,
                    ReasonResource.class,
                    StatusResource.class,
                    CreationRuleResource.class,
                    MeterResource.class,
                    LocationResource.class,
                    DeviceGroupResource.class,
                    IssueTypeResource.class,
                    ActionResource.class,
                    WorkGroupsResource.class,
                    TopIssuesResource.class,
                    IssuePriorityResource.class,
                IssueResource.class,
                HistoryResource.class);
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> hashSet = new HashSet<>();
        hashSet.addAll(super.getSingletons());
        hashSet.add(new HK2Binder());
        return Collections.unmodifiableSet(hashSet);
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
    public void setRestQueryService(RestQueryService restQueryService) {
        this.restQueryService = restQueryService;
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
        this.issueActionService = issueService.getIssueActionService();
        this.issueAssignmentService = issueService.getIssueAssignmentService();
        this.issueCreationService = issueService.getIssueCreationService();
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }

    @Reference
    public void setSearchLocationService(SearchLocationService searchLocationService) {
        this.searchLocationService = searchLocationService;
    }

    @Reference
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(ISSUE_REST_COMPONENT, Layer.REST);
    }

    @Reference
    public void setIssueInfoFactoryService(IssueInfoFactoryService issueInfoFactoryService) {
        this.issueInfoFactoryService = issueInfoFactoryService;
    }

    @Reference
    public void setPropertyValueInfoService(PropertyValueInfoService propertyValueInfoService) {
        this.propertyValueInfoService = propertyValueInfoService;
    }
    
    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Override
    public String getComponentName() {
        return ISSUE_REST_COMPONENT;
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(TranslationKeys.values());
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    class HK2Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(issueService).to(IssueService.class);
            bind(issueAssignmentService).to(IssueAssignmentService.class);
            bind(issueCreationService).to(IssueCreationService.class);
            bind(issueActionService).to(IssueActionService.class);
            bind(userService).to(UserService.class);
            bind(transactionService).to(TransactionService.class);
            bind(restQueryService).to(RestQueryService.class);
            bind(meteringService).to(MeteringService.class);
            bind(meteringGroupsService).to(MeteringGroupsService.class);
            bind(locationService).to(LocationService.class);
            bind(searchLocationService).to(SearchLocationService.class);
            bind(nlsService).to(NlsService.class);
            bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(propertyValueInfoService).to(PropertyValueInfoService.class);
            bind(CreationRuleTemplateInfoFactory.class).to(CreationRuleTemplateInfoFactory.class);
            bind(CreationRuleInfoFactory.class).to(CreationRuleInfoFactory.class);
            bind(CreationRuleActionInfoFactory.class).to(CreationRuleActionInfoFactory.class);
            bind(CreationRuleExclGroupInfoFactory.class).to(CreationRuleExclGroupInfoFactory.class);
            bind(IssueActionInfoFactory.class).to(IssueActionInfoFactory.class);
            bind(IssueResourceHelper.class).to(IssueResourceHelper.class);
            bind(IssueInfoFactory.class).to(IssueInfoFactory.class);
            bind(ConcurrentModificationExceptionFactory.class).to(ConcurrentModificationExceptionFactory.class);
            bind(issueInfoFactoryService).to(IssueInfoFactoryService.class);
            bind(clock).to(Clock.class);
        }
    }

}