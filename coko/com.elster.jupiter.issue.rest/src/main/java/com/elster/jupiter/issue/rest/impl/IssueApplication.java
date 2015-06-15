package com.elster.jupiter.issue.rest.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elster.jupiter.issue.rest.MessageSeeds;
import com.elster.jupiter.issue.rest.impl.resource.ActionResource;
import com.elster.jupiter.issue.rest.impl.resource.AssigneeResource;
import com.elster.jupiter.issue.rest.impl.resource.CreationRuleResource;
import com.elster.jupiter.issue.rest.impl.resource.IssueTypeResource;
import com.elster.jupiter.issue.rest.impl.resource.MeterResource;
import com.elster.jupiter.issue.rest.impl.resource.ReasonResource;
import com.elster.jupiter.issue.rest.impl.resource.RuleResource;
import com.elster.jupiter.issue.rest.impl.resource.StatusResource;
import com.elster.jupiter.issue.rest.response.PropertyUtils;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleActionInfoFactory;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleInfoFactory;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleTemplateInfoFactory;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueAssignmentService;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.google.common.collect.ImmutableSet;

@Component(name = "com.elster.jupiter.issue.rest", 
           service = { Application.class, TranslationKeyProvider.class },
           immediate = true, property = { "alias=/isu", "app=SYS", "name=" + IssueApplication.ISSUE_REST_COMPONENT })
public class IssueApplication extends Application implements TranslationKeyProvider {

    public static final String ISSUE_REST_COMPONENT = "ISR";

    private volatile TransactionService transactionService;
    private volatile RestQueryService restQueryService;
    private volatile UserService userService;
    private volatile IssueService issueService;
    private volatile IssueCreationService issueCreationService;
    private volatile IssueActionService issueActionService;
    private volatile IssueAssignmentService issueAssignmentService;
    private volatile MeteringService meteringService;
    private volatile NlsService nlsService;
    private volatile Thesaurus thesaurus;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.<Class<?>> of(
                    AssigneeResource.class,
                    RuleResource.class,
                    ReasonResource.class,
                    StatusResource.class,
                    CreationRuleResource.class,
                    MeterResource.class,
                    IssueTypeResource.class,
                    ActionResource.class);
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
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(IssueService.COMPONENT_NAME, Layer.REST);
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
            bind(nlsService).to(NlsService.class);
            bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(PropertyUtils.class).to(PropertyUtils.class);
            bind(CreationRuleTemplateInfoFactory.class).to(CreationRuleTemplateInfoFactory.class);
            bind(CreationRuleInfoFactory.class).to(CreationRuleInfoFactory.class);
            bind(CreationRuleActionInfoFactory.class).to(CreationRuleActionInfoFactory.class);
        }
    }
}
