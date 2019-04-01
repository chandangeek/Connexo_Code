/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.devicelifecycle.impl.entity;

import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.issue.devicelifecycle.HistoricalIssueDeviceLifecycle;
import com.energyict.mdc.issue.devicelifecycle.IssueDeviceLifecycleService;
import com.energyict.mdc.issue.devicelifecycle.FailedTransition;
import com.energyict.mdc.issue.devicelifecycle.OpenIssueDeviceLifecycle;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class HistoricalIssueDeviceLifecycleImpl extends IssueDeviceLifecycleImpl implements HistoricalIssueDeviceLifecycle {

    @IsPresent
    private Reference<HistoricalIssue> baseIssue = ValueReference.absent();
    
    @Valid
    private List<HistoricalIssueFailedTransitionImpl> failedTransitions = new ArrayList<>();

    @Inject
    public HistoricalIssueDeviceLifecycleImpl(DataModel dataModel, IssueDeviceLifecycleService issueDeviceLifecycleService) {
        super(dataModel, issueDeviceLifecycleService);
    }

    @Override
    HistoricalIssue getBaseIssue() {
        return baseIssue.orNull();
    }

    public void setIssue(HistoricalIssue issue) {
        this.baseIssue.set(issue);
    }

    void copy(OpenIssueDeviceLifecycle openIssueDeviceLifecycle) {
        for(FailedTransition block : openIssueDeviceLifecycle.getFailedTransitions()) {
            HistoricalIssueFailedTransitionImpl historicalBlock = getDataModel().getInstance(HistoricalIssueFailedTransitionImpl.class);
            historicalBlock.init(this, block);
            failedTransitions.add(historicalBlock);
        }
    }
    
    @Override
    public List<FailedTransition> getFailedTransitions() {
        return Collections.unmodifiableList(failedTransitions);
    }
}
