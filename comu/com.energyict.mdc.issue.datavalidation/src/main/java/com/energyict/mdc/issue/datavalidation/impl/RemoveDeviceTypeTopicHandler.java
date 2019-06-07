/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datavalidation.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.properties.DeviceLifeCycleInDeviceTypeInfo;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;
import static com.energyict.mdc.device.config.properties.DeviceLifeCycleInDeviceTypeInfoValueFactory.DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES;
import static com.energyict.mdc.issue.datavalidation.impl.DataValidationIssueCreationRuleTemplate.DEVICE_CONFIGURATIONS;
import static com.energyict.mdc.issue.datavalidation.impl.DataValidationIssueCreationRuleTemplate.DeviceConfigurationInfo;

@Component(name = "com.energyict.mdc.issue.datavalidation.RemoveDeviceTypeTopicHandler", service = TopicHandler.class, immediate = true)
public class RemoveDeviceTypeTopicHandler implements TopicHandler {
    private IssueService issueService;
    private IssueDataValidationServiceImpl issueDataValidationService;

    public RemoveDeviceTypeTopicHandler() {

    }

    @Inject
    public RemoveDeviceTypeTopicHandler(IssueService issueService, IssueDataValidationServiceImpl issueDataValidationService) {
        this.issueService = issueService;
        this.issueDataValidationService = issueDataValidationService;
    }

    @Override
    public void handle(LocalEvent localEvent) {
        DeviceType deviceType = (DeviceType) localEvent.getSource();
        List<Long> configIds = deviceType.getConfigurations().stream()
                .map(HasId::getId)
                .collect(Collectors.toList());
        List<CreationRule> validationCreationRules = getValidationCreationRules();
        boolean configOfDeviceTypeInUse = validationCreationRules.stream()
                .map(rule -> (List<DeviceConfigurationInfo>) rule.getProperties().get(DEVICE_CONFIGURATIONS))
                .flatMap(Collection::stream)
                .anyMatch(info -> configIds.contains(info.getId()));

        boolean deviceTypeInUse = validationCreationRules.stream()
                .map(rule -> (List)rule.getProperties().get(DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES))
                .filter(list -> !list.isEmpty())
                .flatMap(Collection::stream)
                .anyMatch(info -> ((DeviceLifeCycleInDeviceTypeInfo) info).getDeviceTypeId() == deviceType.getId());

        if(configOfDeviceTypeInUse)
            throw new VetoDeviceTypeDeleteException(issueDataValidationService.thesaurus(), deviceType, MessageSeeds.DEVICE_TYPE_DEVICE_CONFIG_IN_USE);

        if(deviceTypeInUse) {
            throw new VetoDeviceTypeDeleteException(issueDataValidationService.thesaurus(), deviceType, MessageSeeds.DEVICE_TYPE_IN_USE);
        }
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public void setIssueDataValidationService(IssueDataValidationService issueDataValidationService) {
        this.issueDataValidationService = (IssueDataValidationServiceImpl) issueDataValidationService;
    }

    @Override
    public String getTopicMatcher() {
        return "com/energyict/mdc/device/config/devicetype/VALIDATEDELETE";
    }

    public List<CreationRule> getValidationCreationRules() {
        IssueType validationType = issueService.findIssueType("datavalidation").orElse(null);
        List<IssueReason> alarmReasons = issueService.query(IssueReason.class)
                .select(where("issueType").isEqualTo(validationType))
                .stream()
                .collect(Collectors.toList());

        Query<CreationRule> query = issueService.getIssueCreationService().getCreationRuleQuery(IssueReason.class, IssueType.class);
        Condition conditionIssue = where("reason").in(alarmReasons);
        return query.select(conditionIssue, Order.ascending("name"));
    }
}
