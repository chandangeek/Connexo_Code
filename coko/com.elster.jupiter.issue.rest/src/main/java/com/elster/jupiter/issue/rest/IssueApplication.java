package com.elster.jupiter.issue.rest;


import com.elster.jupiter.issue.rest.i18n.MessageSeeds;
import com.elster.jupiter.issue.rest.i18n.TranslationInstaller;
import com.elster.jupiter.issue.rest.resource.*;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleValidationExceptionMapper;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueAssignmentService;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.*;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.rest.util.*;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Component(name = "com.elster.jupiter.issue.rest", service = {Application.class, InstallService.class, TranslationKeyProvider.class}, immediate = true, property = {"alias=/isu", "name=" + IssueApplication.ISSUE_REST_COMPONENT})
public class IssueApplication extends Application implements BinderProvider, InstallService, TranslationKeyProvider {
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
    public Binder getBinder() {
        return new AbstractBinder() {
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
            }
        };
    }

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.<Class<?>>of(
                AssigneeResource.class,
                RuleResource.class,
                ReasonResource.class,
                StatusResource.class,
                CreationRuleResource.class,
                MeterResource.class,
                IssueTypeResource.class,
                ActionResource.class,
                ConstraintViolationExceptionMapper.class,
                LocalizedExceptionMapper.class,
                CreationRuleValidationExceptionMapper.class);
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
    }

    @Reference
    public void setIssueAssignmentService(IssueAssignmentService issueAssignmentService) {
        this.issueAssignmentService = issueAssignmentService;
    }

    @Reference
    public void setIssueCreationService(IssueCreationService issueCreationService) {
        this.issueCreationService = issueCreationService;
    }

    @Reference
    public void setIssueActionService(IssueActionService issueActionService) {
        this.issueActionService = issueActionService;
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
    public final void install() {
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("NLS");
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
}
