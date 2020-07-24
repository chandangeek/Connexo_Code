/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.impl.templates;

import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.properties.DeviceLifeCycleInDeviceTypeInfoValueFactory;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.impl.i18n.TranslationKeys;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;

import static com.energyict.mdc.device.config.properties.DeviceLifeCycleInDeviceTypeInfoValueFactory.DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES;
import static com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription.CONNECTION_LOST;
import static com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription.DEVICE_COMMUNICATION_FAILURE;
import static com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription.UNABLE_TO_CONNECT;

@Component(name = "com.energyict.mdc.issue.datacollection.EventAggregationRuleTemplate",
        property = {"name=" + EventAggregationRuleTemplate.NAME},
        service = CreationRuleTemplate.class, immediate = true)
public class EventAggregationRuleTemplate extends AbstractDataCollectionTemplate {

    public static final String NAME = "EventAggregationRuleTemplate";
    public static final String THRESHOLD = NAME + ".threshold";
    public static final String EVENTTYPE = NAME + ".eventType";

    //For OSGI
    public EventAggregationRuleTemplate() {
    }

    @Inject
    public EventAggregationRuleTemplate(NlsService nlsService, IssueService issueService, IssueDataCollectionService issueDataCollectionService, PropertySpecService propertySpecService, DeviceConfigurationService deviceConfigurationService, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService, MeteringTranslationService meteringTranslationService) {
        this();
        setNlsService(nlsService);
        setIssueService(issueService);
        setIssueDataCollectionService(issueDataCollectionService);
        setPropertySpecService(propertySpecService);
        setDeviceConfigurationService(deviceConfigurationService);
        setMeteringTranslationService(meteringTranslationService);

        activate();
    }

    @Activate
    public void activate() {
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        super.setThesaurus(nlsService.getThesaurus(IssueDataCollectionService.COMPONENT_NAME, Layer.DOMAIN));
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        super.setIssueService(issueService);
    }

    @Reference
    public void setIssueDataCollectionService(IssueDataCollectionService issueDataCollectionService) {
        super.setIssueDataCollectionService(issueDataCollectionService);
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        super.setPropertySpecService(propertySpecService);
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        super.setDeviceConfigurationService(deviceConfigurationService);
    }

    @Reference
    public void setDeviceLifeCycleConfigurationService(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        super.setDeviceLifeCycleConfigurationService(deviceLifeCycleConfigurationService);
    }

    @Reference
    public void setMeteringTranslationService(MeteringTranslationService meteringTranslationService) {
        super.setMeteringTranslationService(meteringTranslationService);
    }

    @Override
    public String getContent() {
        return "package com.energyict.mdc.issue.datacollection\n" +
                "import com.energyict.mdc.issue.datacollection.event.DataCollectionEvent;\n" +
                "global com.elster.jupiter.events.EventService eventService;\n" +
                "global com.elster.jupiter.issue.share.service.IssueCreationService issueCreationService;\n" +
                "global java.util.logging.Logger LOGGER;\n" +
                "rule \"Events from meters of concentrator @{ruleId}\"\n" +
                "when\n" +
                "\tevent : DataCollectionEvent( eventType == \"@{" + EVENTTYPE + "}\" )\n" +
                "\teval( event.hasAssociatedDeviceLifecycleStatesInDeviceTypes(\"@{" + DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES + "}\") == true )\n" +
                "\teval( event.computeCurrentThreshold() > @{" + THRESHOLD + "} )\n" +
                "then\n" +
                "\tSystem.out.println(\"Events from meters of concentrator @{ruleId}\");\n" +
                "\tDataCollectionEvent eventClone = event.cloneForAggregation();\n" +
                "\tissueCreationService.processIssueCreationEvent(@{ruleId}, eventClone);\n" +
                "end";
    }

    @Override
    public String getName() {
        return EventAggregationRuleTemplate.NAME;
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(TranslationKeys.TEMPLATE_EVT_AGGREGATION_NAME).format();
    }

    @Override
    public String getDescription() {
        return getThesaurus().getFormat(TranslationKeys.TEMPLATE_EVT_AGGREGATION_DESCRIPTION).format();
    }

    @Override
    public Optional<? extends Issue> resolveIssue(IssueEvent event) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        Builder<PropertySpec> builder = ImmutableList.builder();
        builder.add(propertySpecService
                .specForValuesOf(new DeviceLifeCycleInDeviceTypeInfoValueFactory(deviceConfigurationService, deviceLifeCycleConfigurationService, meteringTranslationService))
                .named(DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES, TranslationKeys.DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .markMultiValued(";")
                .addValues(deviceConfigurationService.getDeviceLifeCycleInDeviceTypeInfoPossibleValues())
                .markExhaustive(PropertySelectionMode.LIST)
                .finish());
        builder.add(propertySpecService
                .longSpec()
                .named(THRESHOLD, TranslationKeys.PARAMETER_NAME_THRESHOLD)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .addValues(LongStream.rangeClosed(0, 100).boxed().toArray(Long[]::new))
                .markExhaustive()
                .finish());
        EventTypes eventTypes = new EventTypes(getThesaurus(), CONNECTION_LOST, DEVICE_COMMUNICATION_FAILURE, UNABLE_TO_CONNECT);
        builder.add(
            propertySpecService
                .specForValuesOf(new EventTypeValueFactory(eventTypes))
                .named(EVENTTYPE, TranslationKeys.PARAMETER_NAME_EVENT_TYPE)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .addValues(eventTypes.getEventTypes())
                .markExhaustive()
                .finish());
        return builder.build();
    }
}
