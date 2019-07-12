/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.servicecall.impl.entity;

import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.issue.servicecall.HistoricalIssueServiceCall;
import com.elster.jupiter.issue.servicecall.OpenIssueServiceCall;

import javax.inject.Inject;

public class OpenIssueServiceCallImpl extends IssueServiceCallImpl implements OpenIssueServiceCall {

    @IsPresent
    private Reference<OpenIssue> baseIssue = ValueReference.absent();

    @Inject
    public OpenIssueServiceCallImpl(DataModel dataModel) {
        super(dataModel);
    }

    @Override
    OpenIssue getBaseIssue() {
        return baseIssue.orNull();
    }

    public void setIssue(OpenIssue baseIssue) {
        this.baseIssue.set(baseIssue);
    }

    @Override
    public HistoricalIssueServiceCall close(IssueStatus status) {
        HistoricalIssueServiceCallImpl historicalIssueServiceCall = getDataModel().getInstance(HistoricalIssueServiceCallImpl.class);
        historicalIssueServiceCall.copy(this);
        this.delete(); // Remove reference to baseIssue
        HistoricalIssue historicalBaseIssue = getBaseIssue().closeInternal(status);
        historicalIssueServiceCall.setIssue(historicalBaseIssue);
        historicalIssueServiceCall.save();
        return historicalIssueServiceCall;
    }

    @Override
    public HistoricalIssue closeInternal(IssueStatus status) {
        return null;
    }

    @Override
    public void setDevice(com.elster.jupiter.metering.EndDevice device) {
    }

    @Override
    public void setUsagePoint(com.elster.jupiter.metering.UsagePoint usagePoint) {
    }
}
