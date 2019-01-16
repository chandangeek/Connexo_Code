/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.devicelifecycle.impl.entity;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.issue.devicelifecycle.OpenIssueDeviceLifecycle;
import com.energyict.mdc.issue.devicelifecycle.OpenIssueFailedTransition;

import java.time.Instant;

public class OpenIssueFailedTransitionImpl extends FailedTransitionImpl implements OpenIssueFailedTransition {

    @IsPresent
    private Reference<OpenIssueDeviceLifecycle> issue = ValueReference.absent();

    OpenIssueFailedTransitionImpl init(OpenIssueDeviceLifecycle issue, DeviceLifeCycle deviceLifeCycle, StateTransition stateTransition,
                                       State from, State to, Instant modTime, String cause) {
        this.issue.set(issue);
        super.init(deviceLifeCycle, stateTransition, from, to, modTime, cause);
        return this;
    }
}
