/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.devicelifecycle;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.energyict.mdc.common.device.lifecycle.config.DeviceLifeCycle;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

@ProviderType
public interface OpenIssueDeviceLifecycle extends OpenIssue, IssueDeviceLifecycle {
    
    HistoricalIssueDeviceLifecycle close(IssueStatus status);

    void addFailedTransition(DeviceLifeCycle deviceLifeCycle, StateTransition stateTransition,
                             State from, State to, Instant modTime, String cause, Instant createTime);

    void removeFailedTransition(DeviceLifeCycle deviceLifeCycle, StateTransition stateTransition,
                                State from, State to, Instant modTime, String cause);
}
