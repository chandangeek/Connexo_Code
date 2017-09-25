/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.impl.templates;

import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.event.UnregisteredFromGatewayEvent;
import com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription;
import com.energyict.mdc.issue.datacollection.impl.i18n.TranslationKeys;

import com.google.common.collect.ImmutableList;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

@Component(name = "com.energyict.mdc.issue.datacollection.MeterRegistrationRuleTemplate",
        property = {"name=" + MeterRegistrationRuleTemplate.NAME},
        service = CreationRuleTemplate.class, immediate = true)
public class MeterRegistrationRuleTemplate extends AbstractDataCollectionTemplate {
    public static final String NAME = "MeterRegistrationRuleTemplate";
    public static final String DELAY = NAME + ".delay";
    public static final String AUTORESOLUTION = NAME + ".autoresolution";

    //For OSGI
    public MeterRegistrationRuleTemplate() {
    }

    @Inject
    public MeterRegistrationRuleTemplate(IssueDataCollectionService issueDataCollectionService, NlsService nlsService, IssueService issueService, PropertySpecService propertySpecService) {
        this();
        setIssueDataCollectionService(issueDataCollectionService);
        setNlsService(nlsService);
        setIssueService(issueService);
        setPropertySpecService(propertySpecService);
        activate();
    }

    @Activate
    public void activate() {
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();
        builder.add(propertySpecService
                .longSpec()
                .named(DELAY, TranslationKeys.PARAMETER_NAME_DELAY_IN_HOURS)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .setDefaultValue(1L)
                .finish());
        builder.add(propertySpecService
                .booleanSpec()
                .named(AUTORESOLUTION, TranslationKeys.PARAMETER_AUTO_RESOLUTION)
                .fromThesaurus(this.getThesaurus())
                .setDefaultValue(true)
                .finish());
        return builder.build();
    }

    @Override
    public String getName() {
        return MeterRegistrationRuleTemplate.NAME;
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(TranslationKeys.TEMPLATE_UNREGISTERED_FROM_GATEWAY_NAME).format();
    }

    @Override
    public String getDescription() {
        return getThesaurus().getFormat(TranslationKeys.TEMPLATE_UNREGISTERED_FROM_GATEWAY_DESCRIPTION).format();
    }

    @Override
    public String getContent() {
        return "package com.energyict.mdc.issue.datacollection\n" +
                "import com.energyict.mdc.issue.datacollection.event.UnregisteredFromGatewayEvent;\n" +
                "import com.energyict.mdc.issue.datacollection.event.DataCollectionEvent;\n" +
                "import com.energyict.mdc.issue.datacollection.event.RegisteredToGatewayEvent;\n" +
                "global java.util.logging.Logger LOGGER;\n" +
                "global com.elster.jupiter.events.EventService eventService;\n" +
                "global com.elster.jupiter.issue.share.service.IssueCreationService issueCreationService;\n" +
                "rule \"Unregistered from gateway @{ruleId}\"\n" +
                "when\n" +
                "\tevent : UnregisteredFromGatewayEvent(resolveEvent == false)\n" +
                "then\n" +
                "\tLOGGER.info(\"Putting issue on queue by unregistered from gateway rule=@{ruleId}\");\n" +
                "\tlong delay = @{" + DELAY + "} * 3600;\n" +
                "\tevent.setRuleId(@{ruleId});\n" +
                "\teventService.postEvent(\"com/energyict/mdc/issue/datacollection/UNREGISTEREDFROMGATEWAYDELAYED\", event, delay);\n" +
                "end\n" +
                "rule \"Auto-resolution section @{ruleId}\"\n" +
                "when\n" +
                "\tevent : RegisteredToGatewayEvent(@{" + AUTORESOLUTION + "} == 1 )\n" +
                "then\n" +
                "\tLOGGER.info(\"Trying to resolve issue by unregistered from gateway rule=@{ruleId}\");\n" +
                "\tissueCreationService.processIssueResolutionEvent(@{ruleId}, event);\n" +
                "end";
    }

    @Override
    public Optional<? extends Issue> resolveIssue(IssueEvent event) {
        Optional<? extends Issue> issue = event.findExistingIssue();
        if (issue.isPresent() && !issue.get().getStatus().isHistorical()) {
            OpenIssue openIssue = (OpenIssue) issue.get();
            issue = Optional.of(openIssue.close(issueService.findStatus(IssueStatus.RESOLVED).get()));
        }
        return issue;
    }

    @Override
    public void updateIssue(OpenIssue openIssue, IssueEvent event) {
        if (IssueStatus.IN_PROGRESS.equals(openIssue.getStatus().getKey())) {
            openIssue.setStatus(issueService.findStatus(IssueStatus.OPEN).get());
        }
    }

    @Reference
    public void setPropertySpecService(com.energyict.mdc.dynamic.PropertySpecService propertySpecService) {
        super.setPropertySpecService(propertySpecService);
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        super.setThesaurus(nlsService.getThesaurus(IssueDataCollectionService.COMPONENT_NAME, Layer.DOMAIN));
    }

    @Reference
    public void setIssueDataCollectionService(IssueDataCollectionService issueDataCollectionService) {
        super.setIssueDataCollectionService(issueDataCollectionService);
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        super.setIssueService(issueService);
    }


}
