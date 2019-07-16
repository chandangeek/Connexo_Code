/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.servicecall.impl;

import com.elster.jupiter.issue.servicecall.OpenServiceCallIssue;
import com.elster.jupiter.issue.servicecall.impl.i18n.TranslationKeys;
import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.issue.servicecall.ServiceCallIssueService;

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

    public static final String SERVICE_CALL_CONFIGURATIONS = NAME + ".serviceCallConfigurations";

    private volatile ServiceCallIssueService issueServiceCallService;
    private volatile IssueService issueService;
    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;
    private volatile ServiceCallService serviceCallService;
    private Optional<String> appKey = Optional.empty();

    //for OSGI
    public ServiceCallIssueCreationRuleTemplate() {
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
        return "package com.elster.jupiter.servicecall.issue\n" +
                "import ServiceCallStateChangedEvent;\n" +
                "global java.util.logging.Logger LOGGER;\n" +
                "global com.elster.jupiter.events.EventService eventService;\n" +
                "global com.elster.jupiter.issue.share.service.IssueCreationService issueCreationService;\n" +
                "rule \"Service call rule @{ruleId}\"\n" +
                "when\n" +
                "\tevent : ServiceCallStateChangedEvent(servicecallconf in (@{" + SERVICE_CALL_CONFIGURATIONS + "}))\n" +
                "then\n" +
                "\tLOGGER.info(\"Trying to create issue by servicecall rule [id = @{ruleId}]\");\n" +
                "\tissueCreationService.processIssueCreationEvent(@{ruleId}, event);\n" +
                "end\n";
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(ServiceCallIssueService.COMPONENT_NAME, Layer.DOMAIN);
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

    @Override
    public List<PropertySpec> getPropertySpecs() {
        ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();
        builder.add(
                propertySpecService.specForValuesOf(new ServiceCallInfoValueFactory(serviceCallService))
                        .named(TranslationKeys.SERVICE_CALL_TYPE_HANDLER)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .markMultiValued(ServiceCallInfoValueFactory.SEPARATOR)
                        .addValues(serviceCallService.getServiceCallTypes().stream().filter(this::getServiceCallTypeFilter).map(ServiceCallTypeInfo::new).toArray(ServiceCallTypeInfo[]::new))
                        .markExhaustive(PropertySelectionMode.LIST)
                        .finish());
        builder.add(
                propertySpecService.specForValuesOf(new ServiceCallStateInfoValueFactory(thesaurus))
                .named(TranslationKeys.SERVICE_CALL_TYPE_STATE)
                .fromThesaurus(thesaurus)
                .markRequired()
                .markMultiValued(ServiceCallStateInfoValueFactory.SEPARATOR)
                .addValues(Arrays.stream(DefaultState.values()).filter(defaultState -> !defaultState.isOpen()).map(defaultState -> new DefaultStateInfo(defaultState, thesaurus)).collect(Collectors.toList()))
                .markExhaustive(PropertySelectionMode.LIST)
                .finish());
        return builder.build();
    }

    private boolean getServiceCallTypeFilter(ServiceCallType serviceCallType) {
        return appKey.map(s -> !serviceCallType.reservedByApplication().isPresent() ||
                s.equals(serviceCallType.reservedByApplication().get())).orElse(true);
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
        return event.findExistingIssue();
    }

    @Override
    public void setAppKey(String appKey) {
        this.appKey = Optional.of(appKey);
    }

}
