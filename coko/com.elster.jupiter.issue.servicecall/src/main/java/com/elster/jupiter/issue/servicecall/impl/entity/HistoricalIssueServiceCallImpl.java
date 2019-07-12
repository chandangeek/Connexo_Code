/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.servicecall.impl.entity;

import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.issue.servicecall.HistoricalIssueServiceCall;
import com.elster.jupiter.issue.servicecall.OpenIssueServiceCall;

import javax.inject.Inject;

public final class HistoricalIssueServiceCallImpl extends IssueServiceCallImpl implements HistoricalIssueServiceCall {

    @IsPresent
    private Reference<HistoricalIssue> baseIssue = ValueReference.absent();

    @Inject
    public HistoricalIssueServiceCallImpl(DataModel dataModel) {
        super(dataModel);
    }

    @Override
    HistoricalIssue getBaseIssue() {
        return baseIssue.orNull();
    }

    public void setIssue(HistoricalIssue issue) {
        this.baseIssue.set(issue);
    }

    void copy(OpenIssueServiceCall openIssueServiceCall) {
        setNewState(openIssueServiceCall.getNewState());
        setServiceCall(openIssueServiceCall.getServiceCall());
    }
}
