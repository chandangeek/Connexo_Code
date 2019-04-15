/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.impl.templates;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.IssueTypes;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;


import com.energyict.mdc.device.config.DeviceType;

import com.energyict.mdc.device.config.properties.DeviceLifeCycleInDeviceTypeInfo;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.impl.IssueDataCollectionServiceImpl;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;
import static com.energyict.mdc.device.config.properties.DeviceLifeCycleInDeviceTypeInfoValueFactory.DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES;

@Component(name = " com.energyict.mdc.issue.datacollection.RemoveDeviceTypeTopicHandler", service = TopicHandler.class, immediate = true)
public class RemoveDeviceTypeTopicHandler implements TopicHandler{
    private IssueService issueService;
    private IssueDataCollectionServiceImpl issueDataCollectionService;

    public RemoveDeviceTypeTopicHandler() {
    }

    @Inject
    public RemoveDeviceTypeTopicHandler(IssueService issueService, IssueDataCollectionServiceImpl issueDataCollectionService) {
        setIssueService(issueService);
        this.issueDataCollectionService = issueDataCollectionService;
    }

    @Override
    public void handle(LocalEvent localEvent) {
        DeviceType deviceType = (DeviceType) localEvent.getSource();
        List<CreationRule> validationCreationRules = getDataCollectionCreationRules();

        boolean deviceTypeInUse = validationCreationRules.stream()
                .map(rule -> (List)rule.getProperties().get(DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES))
                .filter(list -> list != null && !list.isEmpty())
                .map(list -> list.get(0))
                .map(rule -> (DeviceLifeCycleInDeviceTypeInfo) rule)
                .anyMatch(info ->  info.getDeviceTypeId() == deviceType.getId());

        if(deviceTypeInUse) {
            throw new VetoDeviceTypeDeleteException(issueDataCollectionService.thesaurus(), deviceType);
        }
    }


    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public void setIssueDataValidationService(IssueDataCollectionService issueDataCollectionService) {
        this.issueDataCollectionService = (IssueDataCollectionServiceImpl) issueDataCollectionService;
    }

    @Override
    public String getTopicMatcher() {
        return "com/energyict/mdc/device/config/devicetype/VALIDATEDELETE";
    }


    public List<CreationRule> getDataCollectionCreationRules() {
        IssueType validationType = issueService.findIssueType(IssueTypes.DATA_COLLECTION.getName()).orElse(null);
        List<IssueReason> alarmReasons = issueService.query(IssueReason.class)
                .select(where("issueType").isEqualTo(validationType))
                .stream()
                .collect(Collectors.toList());

        Query<CreationRule> query = issueService.getIssueCreationService().getCreationRuleQuery(IssueReason.class, IssueType.class);
        Condition conditionIssue = where("reason").in(alarmReasons);
        return query.select(conditionIssue, Order.ascending("name"));
    }
}
