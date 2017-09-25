/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.impl.templates;

import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;
import com.energyict.mdc.issue.datacollection.event.DataCollectionEvent;
import com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription;
import com.energyict.mdc.issue.datacollection.impl.i18n.TranslationKeys;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription.CONNECTION_LOST;
import static com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription.DEVICE_COMMUNICATION_FAILURE;
import static com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription.UNABLE_TO_CONNECT;
import static com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription.UNKNOWN_INBOUND_DEVICE;
import static com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription.UNKNOWN_OUTBOUND_DEVICE;

@Component(name = "com.energyict.mdc.issue.datacollection.BasicDatacollectionRuleTemplate",
        property = {"name=" + BasicDataCollectionRuleTemplate.NAME},
        service = CreationRuleTemplate.class,
        immediate = true)
public class BasicDataCollectionRuleTemplate extends AbstractDataCollectionTemplate {
    static final String NAME = "BasicDataCollectionRuleTemplate";

    public static final String EVENTTYPE = NAME + ".eventType";
    public static final String AUTORESOLUTION = NAME + ".autoresolution";
    public static final String INCREASEURGENCY = NAME + ".increaseurgency";

    //for OSGI
    public BasicDataCollectionRuleTemplate() {
    }

    @Inject
    public BasicDataCollectionRuleTemplate(IssueDataCollectionService issueDataCollectionService, NlsService nlsService, IssueService issueService, PropertySpecService propertySpecService) {
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

    @Reference
    public final void setNlsService(NlsService nlsService) {
        this.setThesaurus(nlsService.getThesaurus(IssueDataCollectionService.COMPONENT_NAME, Layer.DOMAIN));
    }

    @Reference
    public void setIssueDataCollectionService(IssueDataCollectionService issueDataCollectionService) {
        super.setIssueDataCollectionService(issueDataCollectionService);
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        super.setIssueService(issueService);
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        super.setPropertySpecService(propertySpecService);
    }

    @Override
    public String getName() {
        return BasicDataCollectionRuleTemplate.NAME;
    }

    @Override
    public String getDescription() {
        return getThesaurus().getFormat(TranslationKeys.BASIC_TEMPLATE_DATACOLLECTION_DESCRIPTION).format();
    }

    @Override
    public String getContent() {
        return "package com.energyict.mdc.issue.datacollection\n" +
                "import com.energyict.mdc.issue.datacollection.event.DataCollectionEvent;\n" +
                "global java.util.logging.Logger LOGGER;\n" +
                "global com.elster.jupiter.events.EventService eventService;\n" +
                "global com.elster.jupiter.issue.share.service.IssueCreationService issueCreationService;\n" +
                "rule \"Basic datacollection rule @{ruleId}\"\n" +
                "when\n" +
                "\tevent : DataCollectionEvent( eventType == \"@{" + EVENTTYPE + "}\", resolveEvent == false )\n" +
                "then\n" +
                "\tLOGGER.info(\"Trying to create issue by basic datacollection rule=@{ruleId}\");\n" +
                "\tissueCreationService.processIssueCreationEvent(@{ruleId}, event);\n" +
                "end\n" +
                "rule \"Auto-resolution section @{ruleId}\"\n" +
                "when\n" +
                "\tevent : DataCollectionEvent( eventType == \"@{" + EVENTTYPE + "}\", resolveEvent == true, @{" + AUTORESOLUTION + "} == 1 )\n" +
                "then\n" +
                "\tLOGGER.info(\"Trying to resolve issue by basic datacollection rule=@{ruleId}\");\n" +
                "\tissueCreationService.processIssueResolutionEvent(@{ruleId}, event);\n" +
                "end";
    }

    @Override
    public void updateIssue(OpenIssue openIssue, IssueEvent event) {
        if (IssueStatus.IN_PROGRESS.equals(openIssue.getStatus().getKey())) {
            openIssue.setStatus(issueService.findStatus(IssueStatus.OPEN).orElseThrow(() ->
                    new IllegalArgumentException(TranslationKeys.ISSUE_REASON_UNKNOWN.getDefaultFormat()) {
                    }));
        }
        openIssue.getRule().getProperties().entrySet().stream().filter(entry -> entry.getKey().equals(INCREASEURGENCY))
                .findFirst().map(found -> (Boolean) found.getValue()).filter(Boolean::booleanValue)
                .ifPresent(increaseUrgency ->
                        openIssue.setPriority(Priority.get(openIssue.getPriority().increaseUrgency(), openIssue.getPriority().getImpact())));
        updateConnectionAttempts(openIssue, event).update();
    }

    @Override
    public Optional<? extends Issue> resolveIssue(IssueEvent event) {
        Optional<? extends Issue> issue = event.findExistingIssue();
        if (issue.isPresent() && !issue.get().getStatus().isHistorical()) {
            OpenIssue openIssue = (OpenIssue) issue.get();
            issue = Optional.of(updateConnectionAttempts(openIssue, event).close(issueService.findStatus(IssueStatus.RESOLVED).get()));
        }
        return issue;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        Builder<PropertySpec> builder = ImmutableList.builder();
        EventTypes eventTypes = new EventTypes(getThesaurus(), CONNECTION_LOST, DEVICE_COMMUNICATION_FAILURE, UNABLE_TO_CONNECT, UNKNOWN_INBOUND_DEVICE, UNKNOWN_OUTBOUND_DEVICE);
        builder.add(propertySpecService
                .specForValuesOf(new EventTypeValueFactory(eventTypes))
                .named(EVENTTYPE, TranslationKeys.PARAMETER_NAME_EVENT_TYPE)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .addValues(eventTypes.getEventTypes())
                .markExhaustive(PropertySelectionMode.COMBOBOX)
                .finish());
        builder.add(propertySpecService
                .booleanSpec()
                .named(AUTORESOLUTION, TranslationKeys.PARAMETER_AUTO_RESOLUTION)
                .fromThesaurus(this.getThesaurus())
                .setDefaultValue(true)
                .finish());
        builder.add(propertySpecService
                .booleanSpec()
                .named(INCREASEURGENCY, TranslationKeys.PARAMETER_INCREASE_URGENCY)
                .fromThesaurus(this.getThesaurus())
                .setDefaultValue(false)
                .finish());
        return builder.build();
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(TranslationKeys.BASIC_TEMPLATE_DATACOLLECTION_NAME).format();
    }

    private OpenIssue updateConnectionAttempts(OpenIssue openIssue, IssueEvent event) {
        if (openIssue instanceof OpenIssueDataCollection && event instanceof DataCollectionEvent) {
            OpenIssueDataCollection dcIssue = OpenIssueDataCollection.class.cast(openIssue);
            dcIssue.setLastConnectionAttemptTimestamp(DataCollectionEvent.class.cast(event).getTimestamp());
            dcIssue.incrementConnectionAttempt();
            return dcIssue;
        }
        return openIssue;
    }

}