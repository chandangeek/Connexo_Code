/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.devicelifecycle.impl.templates;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.issue.devicelifecycle.IssueDeviceLifecycleService;
import com.energyict.mdc.issue.devicelifecycle.impl.DeviceLifecycleIssueCreationRuleTemplate;
import com.energyict.mdc.issue.devicelifecycle.impl.DeviceLifecycleIssueUtil;
import com.energyict.mdc.issue.devicelifecycle.impl.VetoDeviceLifecycleTransitionDeleteException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;

@Component(name = "com.energyict.mdc.issue.devicelifecycle.impl.templates.RemoveTransitionTopicHandler", service = TopicHandler.class, immediate = true)
public class RemoveTransitionTopicHandler implements TopicHandler{
    private IssueService issueService;
    private IssueDeviceLifecycleService issueDeviceLifecycleService;

    public RemoveTransitionTopicHandler() {
    }

    @Inject
    public RemoveTransitionTopicHandler(IssueService issueService, IssueDeviceLifecycleService issueDeviceLifecycleService) {
        setIssueDeviceLifecycleService(issueDeviceLifecycleService);
        setIssueService(issueService);
    }

    @Override
    public void handle(LocalEvent localEvent) {
        Pair<DeviceLifeCycle, StateTransition> pair = (Pair<DeviceLifeCycle, StateTransition>) localEvent.getSource();
        DeviceLifeCycle dlc =pair.getFirst();
        StateTransition transition = pair.getLast();

        DeviceLifecycleIssueUtil
                .getIssueCreationRules(issueService)
                .stream()
                .map(dlcIssueCreationRule -> (List<DeviceLifecycleIssueCreationRuleTemplate.DeviceLifeCycleTransitionPropsInfo>)
                        dlcIssueCreationRule.getProperties()
                        .get(DeviceLifecycleIssueCreationRuleTemplate.DEVICE_LIFECYCLE_TRANSITION_PROPS))
                .flatMap(List::stream)
                .filter(deviceLifeCycleTransitionPropsInfo ->
                        (deviceLifeCycleTransitionPropsInfo.getDeviceLifecycleId() == dlc.getId()) &&
                                (deviceLifeCycleTransitionPropsInfo.getStateTransitionId() == transition.getId()))
                .findFirst()
                .ifPresent(deviceLifeCycleTransitionPropsInfo  -> {
                        throw new VetoDeviceLifecycleTransitionDeleteException(issueDeviceLifecycleService.thesaurus(), transition.getEventType().getSymbol() );
                });

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
        return "com/energyict/mdc/device/lifecycle/config/dlc/transition/delete";
    }
}
