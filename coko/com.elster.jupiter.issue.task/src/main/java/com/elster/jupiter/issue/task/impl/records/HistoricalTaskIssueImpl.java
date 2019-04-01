/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.task.impl.records;

import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.issue.task.entity.HistoricalTaskIssue;
import com.elster.jupiter.issue.task.entity.OpenTaskIssue;

import javax.inject.Inject;

public class HistoricalTaskIssueImpl extends TaskIssueImpl implements HistoricalTaskIssue {

    @IsPresent
    private Reference<HistoricalIssue> baseIssue = ValueReference.absent();

    @Inject
    public HistoricalTaskIssueImpl(DataModel dataModel) {
        super(dataModel);
    }

    @Override
    protected HistoricalIssue getBaseIssue(){
        return baseIssue.orNull();
    }

    void setIssue(HistoricalIssue issue) {
        this.baseIssue.set(issue);
    }

    void copy(OpenTaskIssue source) {
        this.setId(source.getId());
        this.setTaskOccurrence(source.getTaskOccurrence().orElse(null));
        this.setErrorMessage(source.getErrorMessage());
        this.setFailureTime(source.getFailureTime());
    }
}
