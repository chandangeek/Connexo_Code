/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.task.impl.records;

import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.issue.task.entity.HistoricalTaskIssue;
import com.elster.jupiter.issue.task.entity.OpenTaskIssue;

import javax.inject.Inject;

public class OpenTaskIssueImpl extends TaskIssueImpl implements OpenTaskIssue {

    @IsPresent
    private Reference<OpenIssue> baseIssue = ValueReference.absent();

    @Inject
    public OpenTaskIssueImpl(DataModel dataModel) {
        super(dataModel);
    }

    @Override
    protected OpenIssue getBaseIssue(){
        return baseIssue.orNull();
    }

    public void setIssue(OpenIssue baseIssue) {
        this.baseIssue.set(baseIssue);
    }

    public HistoricalTaskIssue close(IssueStatus status) {
        this.delete(); // Remove reference to baseIssue
        HistoricalIssue historicalBaseIssue = getBaseIssue().closeInternal(status);
        HistoricalTaskIssueImpl historicalTaskIssueIssue = getDataModel().getInstance(HistoricalTaskIssueImpl.class);
        historicalTaskIssueIssue.setIssue(historicalBaseIssue);
        historicalTaskIssueIssue.copy(this);
        historicalTaskIssueIssue.save();
        return historicalTaskIssueIssue;
    }
}
