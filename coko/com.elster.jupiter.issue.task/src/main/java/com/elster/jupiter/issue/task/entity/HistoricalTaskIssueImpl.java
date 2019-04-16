/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.task.entity;

import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.task.TaskIssueService;
import com.elster.jupiter.issue.task.HistoricalTaskIssue;
import com.elster.jupiter.issue.task.OpenTaskIssue;
import com.elster.jupiter.issue.task.RelatedTaskOccurrence;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

public class HistoricalTaskIssueImpl extends TaskIssueImpl implements HistoricalTaskIssue {

    @IsPresent
    private Reference<HistoricalIssue> baseIssue = ValueReference.absent();

    @Valid
    private List<HistoricalRelatedTaskOccurrenceImpl> taskOccurrences = new ArrayList<>();

    @Inject
    public HistoricalTaskIssueImpl(DataModel dataModel, TaskIssueService taskIssueService) {
        super(dataModel, taskIssueService);
    }

    @Override
    protected HistoricalIssue getBaseIssue() {
        return baseIssue.orNull();
    }

    void setIssue(HistoricalIssue issue) {
        this.baseIssue.set(issue);
    }

    void copy(OpenTaskIssue source) {
        this.setId(source.getId());
        for (RelatedTaskOccurrence relatedTaskOccurrence : source.getTaskOccurrences()) {
            HistoricalRelatedTaskOccurrenceImpl occurrence = getDataModel().getInstance(HistoricalRelatedTaskOccurrenceImpl.class);
            occurrence.init(this, relatedTaskOccurrence.getTaskOccurrence(), relatedTaskOccurrence.getErrorMessage(), relatedTaskOccurrence.getFailureTime());
            taskOccurrences.add(occurrence);
        }
    }
}
