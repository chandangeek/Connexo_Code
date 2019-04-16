/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.devicelifecycle.impl.templates;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.service.IssueService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.issue.devicelifecycle.IssueDeviceLifecycleService;
import com.energyict.mdc.issue.devicelifecycle.impl.DeviceLifecycleIssueCreationRuleTemplate;
import com.energyict.mdc.issue.devicelifecycle.impl.DeviceLifecycleIssueUtil;
import com.energyict.mdc.issue.devicelifecycle.impl.VetoDeviceTypeDeleteException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;

@Component(name = "com.energyict.mdc.issue.devicelifecycle.impl.templates.RemoveDeviceTypeTopicHandler", service = TopicHandler.class, immediate = true)
public class RemoveDeviceTypeTopicHandler implements TopicHandler {
    private IssueService issueService;
    private IssueDeviceLifecycleService issueDeviceLifecycleService;

    public RemoveDeviceTypeTopicHandler() {
    }

    @Inject
    public RemoveDeviceTypeTopicHandler(IssueService issueService, IssueDeviceLifecycleService issueDeviceLifecycleService) {
        setIssueDeviceLifecycleService(issueDeviceLifecycleService);
        setIssueService(issueService);
    }

    @Override
    public void handle(LocalEvent localEvent) {
        DeviceType deviceType = (DeviceType) localEvent.getSource();
        List<CreationRule> issueCreationRules = DeviceLifecycleIssueUtil.getIssueCreationRules(issueService);
        boolean deviceTypeInUse = issueCreationRules.stream()
                .map(rule -> (List) rule.getProperties().get(DeviceLifecycleIssueCreationRuleTemplate.DEVICE_LIFECYCLE_TRANSITION_PROPS))
                .filter(list -> !list.isEmpty())
                .map(list -> list.get(0))
                .map(rule -> (DeviceLifecycleIssueCreationRuleTemplate.DeviceLifeCycleTransitionPropsInfo) rule)
                .anyMatch(info -> info.getDeviceType().getId() == deviceType.getId());
        if (deviceTypeInUse) {
            throw new VetoDeviceTypeDeleteException(issueDeviceLifecycleService.thesaurus(), deviceType);
        }
    }


    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public void setIssueDeviceLifecycleService(IssueDeviceLifecycleService issueDeviceLifecycleService) {
        this.issueDeviceLifecycleService = issueDeviceLifecycleService;
    }

    @Override
    public String getTopicMatcher() {
        return "com/energyict/mdc/device/config/devicetype/VALIDATEDELETE";
    }
}
