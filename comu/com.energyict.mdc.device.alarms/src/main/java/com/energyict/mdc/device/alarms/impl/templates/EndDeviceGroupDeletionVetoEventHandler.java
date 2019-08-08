/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.alarms.impl.templates;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EventType;
import com.elster.jupiter.metering.groups.GroupEventData;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.impl.DeviceAlarmUtil;
import com.energyict.mdc.device.alarms.impl.event.VetoDeviceGroupDeleteException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Component(name = "com.energyict.mdc.device.alarms.EndDeviceGroupDeletionVetoEventHandler", service = TopicHandler.class, immediate = true)
public class EndDeviceGroupDeletionVetoEventHandler implements TopicHandler {

    private IssueService issueService;
    private DeviceAlarmService deviceAlarmService;
    boolean deviceGroupInUse = false;

    public EndDeviceGroupDeletionVetoEventHandler() {
    }

    @Inject
    public EndDeviceGroupDeletionVetoEventHandler(IssueService issueService, DeviceAlarmService deviceAlarmService) {
        setIssueService(issueService);
        setDeviceAlarmService(deviceAlarmService);
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
    public void handle(LocalEvent localEvent) {
        GroupEventData eventSource = (GroupEventData) localEvent.getSource();
        EndDeviceGroup endDeviceGroup = (EndDeviceGroup) eventSource.getGroup();
        List<CreationRule> alarmCreationRules = DeviceAlarmUtil.getAlarmCreationRules(issueService);
        alarmCreationRules
                .forEach(rule -> {
                    List<Long> endDeviceGroupIds = new ArrayList<>();
                    rule.getProperties().entrySet().stream().filter(entry -> entry.getKey().equals(BasicDeviceAlarmRuleTemplate.DEVICE_IN_GROUP))
                            .findFirst().ifPresent(element ->
                            ((ArrayList<BasicDeviceAlarmRuleTemplate.DeviceGroupInfo>) (element.getValue())).forEach(value -> endDeviceGroupIds.add(Long.parseLong(value.getId()))));
                    if (endDeviceGroupIds.contains(endDeviceGroup.getId())) {
                        throw new VetoDeviceGroupDeleteException(deviceAlarmService.thesaurus(), endDeviceGroup);
                    }
                });

    }

    @Override
    public String getTopicMatcher() {
        return EventType.ENDDEVICEGROUP_VALIDATE_DELETED.topic();
    }
}
