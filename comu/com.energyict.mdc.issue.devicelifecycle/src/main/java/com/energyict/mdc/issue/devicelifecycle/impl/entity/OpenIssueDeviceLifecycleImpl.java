/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.devicelifecycle.impl.entity;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.issue.devicelifecycle.FailedTransition;
import com.energyict.mdc.issue.devicelifecycle.HistoricalIssueDeviceLifecycle;
import com.energyict.mdc.issue.devicelifecycle.IssueDeviceLifecycleService;
import com.energyict.mdc.issue.devicelifecycle.OpenIssueDeviceLifecycle;

import com.google.common.collect.Range;

import javax.inject.Inject;
import javax.validation.Valid;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class OpenIssueDeviceLifecycleImpl extends IssueDeviceLifecycleImpl implements OpenIssueDeviceLifecycle {

    @IsPresent
    private Reference<OpenIssue> baseIssue = ValueReference.absent();

    @Valid
    private List<OpenIssueFailedTransitionImpl> failedTransitions = new ArrayList<>();

    @Inject
    public OpenIssueDeviceLifecycleImpl(DataModel dataModel, IssueDeviceLifecycleService issueDeviceLifecycleService) {
        super(dataModel, issueDeviceLifecycleService);
    }

    @Override
    OpenIssue getBaseIssue() {
        return baseIssue.orNull();
    }

    public void setIssue(OpenIssue baseIssue) {
        this.baseIssue.set(baseIssue);
    }

    @Override
    public HistoricalIssueDeviceLifecycle close(IssueStatus status) {
        HistoricalIssueDeviceLifecycleImpl historicalDeviceLifecycleIssue = getDataModel().getInstance(HistoricalIssueDeviceLifecycleImpl.class);
        historicalDeviceLifecycleIssue.copy(this);
        this.delete(); // Remove reference to baseIssue
        HistoricalIssue historicalBaseIssue = getBaseIssue().closeInternal(status);
        historicalDeviceLifecycleIssue.setIssue(historicalBaseIssue);
        historicalDeviceLifecycleIssue.save();
        return historicalDeviceLifecycleIssue;
    }

    @Override
    public void addFailedTransition(DeviceLifeCycle deviceLifeCycle, StateTransition stateTransition,
                                    State from, State to, Instant modTime, String cause, Instant createTime) {
        createNewFailedTransition(deviceLifeCycle, stateTransition, from, to, modTime, cause, createTime);
    }

    @Override
    public void removeFailedTransition(DeviceLifeCycle deviceLifeCycle, StateTransition stateTransition, State from, State to, Instant modTime, String cause) {
        Optional<OpenIssueFailedTransitionImpl> failedTransition = failedTransitions.stream()
                .filter(transition -> transition.getLifecycle().getId() == deviceLifeCycle.getId())
                .filter(transition -> transition.getTransition().getId() == stateTransition.getId())
                .filter(transition -> transition.getFrom().getId() == stateTransition.getId())
                .filter(transition -> transition.getTo().getId() == stateTransition.getId())
                .filter(transition -> transition.getOccurrenceTime().equals(modTime))
                .filter(transition -> transition.getCause().equals(cause))
                .findFirst();

        failedTransition.ifPresent(openIssueFailedTransition -> failedTransitions.remove(openIssueFailedTransition));
    }


    @Override
    public List<FailedTransition> getFailedTransitions() {
        return Collections.unmodifiableList(failedTransitions);
    }

    private void createNewFailedTransition(DeviceLifeCycle deviceLifeCycle, StateTransition stateTransition,
                                           State from, State to, Instant modTime, String cause, Instant createTime) {
        OpenIssueFailedTransitionImpl failedTransition = getDataModel().getInstance(OpenIssueFailedTransitionImpl.class);
        failedTransition.init(this, deviceLifeCycle, stateTransition, from, to, modTime, cause, createTime);
        failedTransitions.add(failedTransition);
    }
}
