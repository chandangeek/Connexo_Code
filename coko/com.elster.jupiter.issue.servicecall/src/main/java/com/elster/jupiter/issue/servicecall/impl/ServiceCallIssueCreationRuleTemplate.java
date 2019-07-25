/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.servicecall.impl;

import com.elster.jupiter.issue.servicecall.OpenServiceCallIssue;
import com.elster.jupiter.issue.servicecall.ServiceCallIssueService;
import com.elster.jupiter.issue.servicecall.impl.i18n.TranslationKeys;
import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;

import com.google.common.collect.ImmutableList;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component(name = "ServiceCallIssueCreationRuleTemplate",
        property = {"name=" + ServiceCallIssueCreationRuleTemplate.NAME},
        service = CreationRuleTemplate.class, immediate = true)
public class ServiceCallIssueCreationRuleTemplate implements CreationRuleTemplate {

    static final String NAME = "ServiceCallIssueCreationRuleTemplate";

    public static final String AUTORESOLUTION = NAME + ".autoresolution";

    private volatile ServiceCallIssueService issueServiceCallService;
    private volatile IssueService issueService;
    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;
    private volatile ServiceCallService serviceCallService;
    private volatile ThreadPrincipalService threadPrincipalService;

    //for OSGI
    public ServiceCallIssueCreationRuleTemplate() {ArmServiceCallHandler
    }

    @Inject
    public ServiceCallIssueCreationRuleTemplate(ServiceCallIssueService issueServiceCallService, IssueService issueService, PropertySpecService propertySpecService,
                                                ServiceCallService serviceCallService,
                                                NlsService nlsService) {
        this();
        setIssueServiceCallService(issueServiceCallService);
        setIssueService(issueService);
        setNlsService(nlsService);
        setPropertySpecService(propertySpecService);
        setServiceCallService(serviceCallService);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDisplayName() {
        return TranslationKeys.SERVICE_CALL_ISSUE_RULE_TEMPLATE_NAME.getTranslated(thesaurus);
    }

    @Override
    public String getDescription() {
        return TranslationKeys.SERVICE_CALL_ISSUE_RULE_TEMPLATE_DESCRIPTION.getTranslated(thesaurus);
    }

    @Override
    public String getContent() {
        return "package com.elster.jupiter.issue.servicecall\n" +
                "import com.elster.jupiter.issue.servicecall.impl.event.ServiceCallStateChangedEvent;\n" +
                "global java.util.logging.Logger LOGGER;\n" +
                "global com.elster.jupiter.events.EventService eventService;\n" +
                "global com.elster.jupiter.issue.share.service.IssueCreationService issueCreationService;\n" +
                "rule \"Service call rule @{ruleId}\"\n" +
                "when\n" +
                "\tevent : ServiceCallStateChangedEvent(stateId in (@{" + TranslationKeys.SERVICE_CALL_TYPE_STATE.getKey() + "}), " +
                " serviceCallTypeId in (@{" + TranslationKeys.SERVICE_CALL_TYPE.getKey() + "}))\n" +
                "then\n" +
                "\tLOGGER.info(\"Trying to create issue by servicecall rule [id = @{ruleId}]\");\n" +
                "\tissueCreationService.processIssueCreationEvent(@{ruleId}, event);\n" +
                "end\n" +
                "rule \"Auto-resolution section @{ruleId}\"\n" +
                "when\n" +
                "\tevent : ServiceCallStateChangedEvent(stateId == " + DefaultState.SUCCESSFUL.ordinal() + "), " +
                " serviceCallTypeId in (@{" + TranslationKeys.SERVICE_CALL_TYPE.getKey() + "})," +
                "@{" + AUTORESOLUTION + "} == 1" +
                ")\n" +
                "then\n" +
                "\tLOGGER.info(\"Trying to resolve service call issue rule=@{ruleId}\");\n" +
                "\tissueCreationService.processIssueResolutionEvent(@{ruleId}, event);\n" +
                "end\n";
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(ServiceCallIssueService.COMPONENT_NAME, Layer.DOMAIN)
                .join(nlsService.getThesaurus(ServiceCallService.COMPONENT_NAME, Layer.DOMAIN));
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public void setIssueServiceCallService(ServiceCallIssueService issueServiceCallService) {
        this.issueServiceCallService = issueServiceCallService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();
        builder.add(
                propertySpecService.specForValuesOf(new ServiceCallTypeInfoValueFactory(serviceCallService))
                        .named(TranslationKeys.SERVICE_CALL_TYPE)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .markMultiValued(ServiceCallTypeInfoValueFactory.SEPARATOR)
                        .addValues(serviceCallService.getServiceCallTypes().stream().filter(this::getServiceCallTypeFilter).map(ServiceCallTypeInfo::new).toArray(ServiceCallTypeInfo[]::new))
                        .markExhaustive(PropertySelectionMode.LIST)
                        .finish());
        builder.add(
                propertySpecService.specForValuesOf(new ServiceCallStateInfoValueFactory(thesaurus))
                .named(TranslationKeys.SERVICE_CALL_TYPE_STATE)
                .fromThesaurus(thesaurus)
                .markRequired()
                .markMultiValued(ServiceCallStateInfoValueFactory.SEPARATOR)
                .addValues(Arrays.stream(DefaultState.values()).filter(defaultState -> !defaultState.isOpen() && DefaultState.SUCCESSFUL != defaultState).map(defaultState -> new DefaultStateInfo(defaultState, thesaurus)).collect(Collectors.toList()))
                .markExhaustive(PropertySelectionMode.LIST)
                .finish());
        builder.add(propertySpecService
                .booleanSpec()
                .named(AUTORESOLUTION, TranslationKeys.PARAMETER_AUTO_RESOLUTION)
                .fromThesaurus(thesaurus)
                .setDefaultValue(true)
                .finish());
        return builder.build();
    }

    private boolean getServiceCallTypeFilter(ServiceCallType serviceCallType) {
        if (serviceCallType.getReservedByApplication().isPresent()) {
           return threadPrincipalService.getApplicationName().equals(serviceCallType.getReservedByApplication().get());
        }
        return true;
    }

    @Override
    public IssueType getIssueType() {
        return issueService.findIssueType(ServiceCallIssueService.ISSUE_TYPE_NAME).orElse(null);
    }

    @Override
    public OpenServiceCallIssue createIssue(OpenIssue baseIssue, IssueEvent issueEvent) {
        return issueServiceCallService.createIssue(baseIssue, issueEvent);
    }

    @Override
    public Optional<? extends Issue> resolveIssue(IssueEvent event) {
        Optional<? extends Issue> issue = event.findExistingIssue();
        if (issue.isPresent() && !issue.get().getStatus().isHistorical()) {
            OpenIssue openIssue = (OpenIssue) issue.get();
            issue = Optional.of(openIssue.close(issueService.findStatus(IssueStatus.RESOLVED)
                    .get()));
        }
        return issue;
    }

}
