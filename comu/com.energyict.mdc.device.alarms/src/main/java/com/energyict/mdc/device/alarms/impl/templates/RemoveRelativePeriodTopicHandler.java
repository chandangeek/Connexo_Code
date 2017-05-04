/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.impl.templates;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.impl.event.VetoDeviceTypeDeleteException;
import com.energyict.mdc.device.alarms.impl.event.VetoRelativePeriodDeleteException;
import com.energyict.mdc.device.config.DeviceType;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

@Component(name = "com.energyict.mdc.device.alarms.RemoveRelativePeriodTopicHandler", service = TopicHandler.class, immediate = true)
public class RemoveRelativePeriodTopicHandler implements TopicHandler{
    private IssueService issueService;
    private DeviceAlarmService deviceAlarmService;

    public RemoveRelativePeriodTopicHandler() {
    }

    @Inject
    public RemoveRelativePeriodTopicHandler(IssueService issueService, DeviceAlarmService deviceAlarmService) {
        setDeviceAlarmService(deviceAlarmService);
        setIssueService(issueService);
    }

    @Override
    public void handle(LocalEvent localEvent) {
        RelativePeriod relativePeriod = (RelativePeriod) localEvent.getSource();
        List<CreationRule> alarmCreationRules = getAlarmCreationRules();
        boolean deviceTypeInUse = alarmCreationRules.stream()
                .map(rule -> (BasicDeviceAlarmRuleTemplate.RelativePeriodWithCountInfo)rule.getProperties().get(BasicDeviceAlarmRuleTemplate.THRESHOLD))
                .anyMatch(info -> info.getRelativePeriodId() == relativePeriod.getId());
        if(deviceTypeInUse) {
            throw new VetoRelativePeriodDeleteException(deviceAlarmService.thesaurus(), relativePeriod);
        }
    }

    private List<CreationRule> getAlarmCreationRules() {
        IssueType alarmType = issueService.findIssueType("devicealarm").orElse(null);
        List<IssueReason> alarmReasons = issueService.query(IssueReason.class)
                .select(where("issueType").isEqualTo(alarmType))
                .stream()
                .collect(Collectors.toList());

        Query<CreationRule> query = issueService.getIssueCreationService().getCreationRuleQuery(IssueReason.class, IssueType.class);
        Condition conditionIssue = where("reason").in(alarmReasons);
        return query.select(conditionIssue, Order.ascending("name"));
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public void setDeviceAlarmService(DeviceAlarmService deviceAlarmService) {
        this.deviceAlarmService = deviceAlarmService;
    }

    @Override
    public String getTopicMatcher() {
        return "com/elster/jupiter/time/relativeperiod/DELETED";
    }
}
