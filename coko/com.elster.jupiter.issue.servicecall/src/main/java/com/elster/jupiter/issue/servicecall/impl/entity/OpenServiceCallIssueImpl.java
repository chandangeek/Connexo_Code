/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.servicecall.impl.entity;

import com.elster.jupiter.issue.servicecall.HistoricalServiceCallIssue;
import com.elster.jupiter.issue.servicecall.OpenServiceCallIssue;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;

public class OpenServiceCallIssueImpl extends ServiceCallIssueImpl implements OpenServiceCallIssue {

    @IsPresent
    private Reference<OpenIssue> baseIssue = ValueReference.absent();

    @Inject
    public OpenServiceCallIssueImpl(DataModel dataModel) {
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
    public HistoricalServiceCallIssue close(IssueStatus status) {
        HistoricalServiceCallIssueImpl historicalIssueServiceCall = getDataModel().getInstance(HistoricalServiceCallIssueImpl.class);
        historicalIssueServiceCall.copy(this);
        this.delete(); // Remove reference to baseIssue
        HistoricalIssue historicalBaseIssue = getBaseIssue().closeInternal(status);
        historicalIssueServiceCall.setIssue(historicalBaseIssue);
        historicalIssueServiceCall.save();
        return historicalIssueServiceCall;
    }
}
