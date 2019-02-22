/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.devicelifecycle.impl.entity;

import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.issue.devicelifecycle.FailedTransition;
import com.energyict.mdc.issue.devicelifecycle.HistoricalIssueDeviceLifecycle;
import com.energyict.mdc.issue.devicelifecycle.HistoricalIssueFailedTransition;

public class HistoricalIssueFailedTransitionImpl extends FailedTransitionImpl implements HistoricalIssueFailedTransition {

    @IsPresent
    private Reference<HistoricalIssueDeviceLifecycle> issue = ValueReference.absent();

    HistoricalIssueFailedTransitionImpl init(HistoricalIssueDeviceLifecycle issue, FailedTransition transition) {
        this.issue.set(issue);
        super.init(transition.getLifecycle(),
                transition.getTransition(),
                transition.getFrom(),
                transition.getTo(),
                transition.getOccurrenceTime(),
                transition.getCause(),
                transition.getCreateTime());
        return this;
    }
}
