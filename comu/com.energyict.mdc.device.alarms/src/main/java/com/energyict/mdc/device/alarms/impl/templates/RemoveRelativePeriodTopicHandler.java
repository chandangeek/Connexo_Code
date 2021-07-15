/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.impl.templates;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.time.RelativePeriod;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.impl.DeviceAlarmUtil;
import com.energyict.mdc.device.alarms.impl.event.VetoRelativePeriodDeleteException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;

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
        List<CreationRule> alarmCreationRules = DeviceAlarmUtil.getAlarmCreationRules(issueService);
        boolean deviceTypeInUse = alarmCreationRules.stream()
                .map(rule -> (BasicDeviceAlarmRuleTemplate.RelativePeriodWithCountInfo)rule.getProperties().get(BasicDeviceAlarmRuleTemplate.THRESHOLD))
                .filter(Objects::nonNull)
                .anyMatch(info -> info.getRelativePeriodId() == relativePeriod.getId());

        if(deviceTypeInUse) {
            throw new VetoRelativePeriodDeleteException(deviceAlarmService.thesaurus(), relativePeriod);
        }
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
