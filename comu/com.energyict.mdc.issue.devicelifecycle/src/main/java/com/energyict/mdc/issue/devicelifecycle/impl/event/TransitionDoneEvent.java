/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.devicelifecycle.impl.event;

import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.issue.devicelifecycle.IssueDeviceLifecycleService;

import com.google.inject.Inject;

import java.util.Map;

public class TransitionDoneEvent extends DeviceLifecycleEvent {

    @Inject
    public TransitionDoneEvent(Thesaurus thesaurus, DeviceService deviceService, IssueDeviceLifecycleService issueDeviceLifecycleService, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService, IssueService issueService, MeteringService meteringService) {
        super(thesaurus, deviceService, issueDeviceLifecycleService, deviceLifeCycleConfigurationService, issueService, meteringService);
    }

    @Override
    public void init(Map<?, ?> jsonPayload) {
        // do nothing, this event shouldn't produce any issues
    }

    @Override
    public void apply(Issue issue) {
        // do nothing, this event shouldn't produce any issues
    }

    @Override
    public boolean isResolveEvent() {
        return true;
    }
}
