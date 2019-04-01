/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.devicelifecycle.impl.event;

import com.elster.jupiter.issue.share.UnableToCreateEventException;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.issue.devicelifecycle.IssueDeviceLifecycleService;
import com.energyict.mdc.issue.devicelifecycle.OpenIssueDeviceLifecycle;
import com.energyict.mdc.issue.devicelifecycle.impl.MessageSeeds;

import com.google.inject.Inject;

import java.time.Instant;
import java.util.Map;

import static com.elster.jupiter.util.Checks.is;

public class TransitionRemovedEvent extends DeviceLifecycleEvent {

    private Instant modTime;

    @Inject
    public TransitionRemovedEvent(Thesaurus thesaurus, DeviceService deviceService, IssueDeviceLifecycleService issueDeviceLifecycleService, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService, IssueService issueService, MeteringService meteringService) {
        super(thesaurus, deviceService, issueDeviceLifecycleService, deviceLifeCycleConfigurationService, issueService, meteringService);
    }

    @Override
    public void apply(Issue issue) {
        if (issue instanceof OpenIssueDeviceLifecycle) {
            OpenIssueDeviceLifecycle deviceLifecycleIssue = (OpenIssueDeviceLifecycle) issue;
            deviceLifecycleIssue.removeFailedTransition(getDeviceLifecycle().get(), getTransition().get(), getFrom().get(), getTo().get(), modTime, cause);
        }
    }

    @Override
    public void init(Map<?, ?> jsonPayload) {
        try {
            this.device = ((Number) jsonPayload.get("device")).longValue();
            this.lifecycle = ((Number) jsonPayload.get("lifecycle")).longValue();
            this.transition = ((Number) jsonPayload.get("transition")).longValue();
            this.from = ((Number) jsonPayload.get("from")).longValue();
            this.to = ((Number) jsonPayload.get("to")).longValue();
            this.cause = (String) jsonPayload.get("cause");
            this.modTime = Instant.ofEpochMilli(((Number) jsonPayload.get("modTime")).longValue());

        } catch (Exception e) {
            throw new UnableToCreateEventException(getThesaurus(), MessageSeeds.UNABLE_TO_CREATE_EVENT, jsonPayload.toString());
        }
    }

    @Override
    public boolean isResolveEvent(){
        return true;
    }
}
