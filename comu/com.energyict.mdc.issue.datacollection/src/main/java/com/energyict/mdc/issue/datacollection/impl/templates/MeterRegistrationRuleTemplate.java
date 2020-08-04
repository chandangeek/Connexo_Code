/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.impl.templates;

import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.properties.DeviceLifeCycleInDeviceTypeInfoValueFactory;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.impl.i18n.TranslationKeys;

import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static com.energyict.mdc.device.config.properties.DeviceLifeCycleInDeviceTypeInfoValueFactory.DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES;

public class MeterRegistrationRuleTemplate extends AbstractDataCollectionTemplate {
    public static final String NAME = "MeterRegistrationRuleTemplate";
    public static final String DELAY = NAME + ".delay";
    public static final String AUTORESOLUTION = NAME + ".autoresolution";

    @Inject
    public MeterRegistrationRuleTemplate(IssueDataCollectionService issueDataCollectionService,
                                         Thesaurus thesaurus,
                                         IssueService issueService,
                                         PropertySpecService propertySpecService,
                                         DeviceConfigurationService deviceConfigurationService,
                                         DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService,
                                         MeteringTranslationService meteringTranslationService) {
        super.setIssueDataCollectionService(issueDataCollectionService);
        super.setThesaurus(thesaurus);
        super.setIssueService(issueService);
        super.setPropertySpecService(propertySpecService);
        super.setDeviceConfigurationService(deviceConfigurationService);
        super.setDeviceLifeCycleConfigurationService(deviceLifeCycleConfigurationService);
        super.setMeteringTranslationService(meteringTranslationService);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();
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
                "\teval( event.hasAssociatedDeviceLifecycleStatesInDeviceTypes(\"@{" + DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES + "}\") == true )\n" +
                "then\n" +
                "\tLOGGER.info(\"Putting issue on queue by unregistered from gateway rule=@{ruleId}\");\n" +
                "\tlong delay = @{" + DELAY + "} * 3600;\n" +
                "\tevent.setCreationRule(@{ruleId});\n" +
                "\teventService.postEvent(\"com/energyict/mdc/issue/datacollection/UNREGISTEREDFROMGATEWAYDELAYED\", event, delay);\n" +
                "end\n" +
                "rule \"Auto-resolution section @{ruleId}\"\n" +
                "when\n" +
                "\tevent : RegisteredToGatewayEvent(@{" + AUTORESOLUTION + "} == 1 )\n" +
                "then\n" +
                "\tLOGGER.info(\"Trying to resolve issue by unregistered from gateway rule=@{ruleId}\");\n" +
                "\tevent.setCreationRule(@{ruleId});\n" +
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
}
