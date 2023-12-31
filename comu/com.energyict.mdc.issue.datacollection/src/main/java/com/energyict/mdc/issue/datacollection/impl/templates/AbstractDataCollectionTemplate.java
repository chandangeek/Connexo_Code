/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.impl.templates;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.properties.DeviceLifeCycleInDeviceTypeInfo;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;

import javax.inject.Inject;
import java.time.Clock;
import java.util.List;
import java.util.Optional;

import static com.elster.jupiter.util.conditions.Where.where;
import static com.energyict.mdc.device.config.properties.DeviceLifeCycleInDeviceTypeInfoValueFactory.DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES;

public abstract class AbstractDataCollectionTemplate implements CreationRuleTemplate {

    protected volatile IssueService issueService;
    protected volatile IssueDataCollectionService issueDataCollectionService;
    protected volatile PropertySpecService propertySpecService;
    protected volatile DeviceConfigurationService deviceConfigurationService;
    protected volatile DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    protected volatile MeteringTranslationService meteringTranslationService;
    protected volatile Thesaurus thesaurus;
    protected volatile TimeService timeService;
    protected volatile Clock clock;

    public AbstractDataCollectionTemplate() {
    }

    @Inject
    protected AbstractDataCollectionTemplate(IssueService issueService, IssueDataCollectionService issueDataCollectionService, Thesaurus thesaurus, PropertySpecService propertySpecService, DeviceConfigurationService deviceConfigurationService, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService,MeteringTranslationService meteringTranslationService, TimeService timeService, Clock clock) {
        this.issueService = issueService;
        this.issueDataCollectionService = issueDataCollectionService;
        this.propertySpecService = propertySpecService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
        this.meteringTranslationService = meteringTranslationService;
        this.thesaurus = thesaurus;
        this.timeService = timeService;
        this.clock = clock;
    }

    public Thesaurus getThesaurus() {
        return thesaurus;
    }

    @Override
    public OpenIssueDataCollection createIssue(OpenIssue baseIssue, IssueEvent event) {
        return issueDataCollectionService.createIssue(baseIssue, event);
    }

    @Override
    public IssueType getIssueType() {
        return issueService.findIssueType(IssueDataCollectionService.DATA_COLLECTION_ISSUE).get();
    }

    protected void setThesaurus(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    protected void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    protected void setIssueDataCollectionService(IssueDataCollectionService issueDataCollectionService) {
        this.issueDataCollectionService = issueDataCollectionService;
    }

    protected void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    protected void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    protected void setDeviceLifeCycleConfigurationService(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }

    protected void setMeteringTranslationService(MeteringTranslationService meteringTranslationService) {
        this.meteringTranslationService =  meteringTranslationService;
    }

    protected void setTimeService(final TimeService timeService) {
        this.timeService = timeService;
    }

    protected void setClock(final Clock clock) {
        this.clock = clock;
    }

    protected List<CreationRule> getExistingCreationRules() {
        Query<CreationRule> query = issueService.getIssueCreationService().getCreationRuleQuery(IssueReason.class, IssueType.class);
        Condition conditionIssue = where("template").isEqualTo(getName());
        return query.select(conditionIssue, Order.ascending("name"));
    }

    @Override
    public Optional<CreationRule> getCreationRuleWhichUsesDeviceType(Long deviceTypeId)
    {
        for (CreationRule creationRule : getExistingCreationRules()) {
            Object lifecycleStates = creationRule.getProperties().get(DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES);
            if(lifecycleStates instanceof List && ((List)lifecycleStates).stream()
                    .anyMatch(propertySpec -> (propertySpec instanceof DeviceLifeCycleInDeviceTypeInfo) &&
                            ((DeviceLifeCycleInDeviceTypeInfo)propertySpec).getDeviceTypeId() == deviceTypeId))
                return Optional.of(creationRule);
        }
        return Optional.empty();
    }

}
