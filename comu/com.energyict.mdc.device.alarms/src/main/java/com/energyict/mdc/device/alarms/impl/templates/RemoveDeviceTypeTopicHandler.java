/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.impl.templates;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.service.IssueService;


import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.impl.DeviceAlarmUtil;
import com.energyict.mdc.device.alarms.impl.event.VetoDeviceTypeDeleteException;
import com.energyict.mdc.device.config.DeviceType;

import com.energyict.mdc.device.config.properties.DeviceLifeCycleInDeviceTypeInfo;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;

import static com.energyict.mdc.device.config.properties.DeviceLifeCycleInDeviceTypeInfoValueFactory.DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES;

@Component(name = "com.energyict.mdc.device.alarms.RemoveDeviceTypeTopicHandler", service = TopicHandler.class, immediate = true)
public class RemoveDeviceTypeTopicHandler implements TopicHandler{
    private IssueService issueService;
    private DeviceAlarmService deviceAlarmService;

    public RemoveDeviceTypeTopicHandler() {
    }

    @Inject
    public RemoveDeviceTypeTopicHandler(IssueService issueService, DeviceAlarmService deviceAlarmService) {
        setDeviceAlarmService(deviceAlarmService);
        setIssueService(issueService);
    }

    @Override
    public void handle(LocalEvent localEvent) {
        DeviceType deviceType = (DeviceType) localEvent.getSource();
        List<CreationRule> alarmCreationRules = DeviceAlarmUtil.getAlarmCreationRules(issueService);
        boolean deviceTypeInUse = alarmCreationRules.stream()
                .map(rule -> (List)rule.getProperties().get(DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES))
                .filter(list -> !list.isEmpty())
                .map(list -> list.get(0))
                .map(rule -> (DeviceLifeCycleInDeviceTypeInfo) rule)
                .anyMatch(info -> info.getDeviceType().getId() == deviceType.getId());
        if(deviceTypeInUse) {
            throw new VetoDeviceTypeDeleteException(deviceAlarmService.thesaurus(), deviceType);
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
        return "com/energyict/mdc/device/config/devicetype/VALIDATEDELETE";
    }
}
