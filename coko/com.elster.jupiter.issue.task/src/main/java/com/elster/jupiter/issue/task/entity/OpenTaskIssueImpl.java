/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.task.entity;

import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.task.TaskIssueService;
import com.elster.jupiter.issue.task.HistoricalTaskIssue;
import com.elster.jupiter.issue.task.OpenRelatedTaskOccurrence;
import com.elster.jupiter.issue.task.OpenTaskIssue;
import com.elster.jupiter.issue.task.RelatedTaskOccurrence;
import com.elster.jupiter.issue.task.impl.i18n.MessageSeeds;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.tasks.TaskOccurrence;

import javax.inject.Inject;
import javax.validation.Valid;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class OpenTaskIssueImpl extends TaskIssueImpl implements OpenTaskIssue {

    @IsPresent
    private Reference<OpenIssue> baseIssue = ValueReference.absent();

    @Valid
    private List<OpenRelatedTaskOccurrence> taskOccurrences = new ArrayList<>();

    @Inject
    public OpenTaskIssueImpl(DataModel dataModel, TaskIssueService taskIssueService) {
        super(dataModel, taskIssueService);
    }

    @Override
    protected OpenIssue getBaseIssue() {
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

    @Override
    public void addTaskOccurrence(TaskOccurrence taskOccurrence, String errorMessage, Instant failureTime) {
        if (!getTaskOccurrence(taskOccurrence, errorMessage, failureTime).isPresent())
            createNewRelatedTaskOccurrence(taskOccurrence, errorMessage, failureTime);
    }

    private Optional<OpenRelatedTaskOccurrence> getTaskOccurrence(TaskOccurrence taskOccurrence, String errorMessage, Instant failureTime) {
        return  taskOccurrences.stream()
                .filter(occurrence -> occurrence.getTaskOccurrence().equals(taskOccurrence))
                .filter(occurrence -> occurrence.getErrorMessage().equals(errorMessage))
                .filter(occurrence -> occurrence.getFailureTime().equals(failureTime))
                .findFirst();
    }

    @Override
    public void removeTaskOccurrence(TaskOccurrence taskOccurrence, String errorMessage, Instant failureTime) {
        Optional<OpenRelatedTaskOccurrence> relatedTaskOccurrence = getTaskOccurrence(taskOccurrence, errorMessage, failureTime);
        relatedTaskOccurrence.ifPresent(openIssueFailedTransition -> taskOccurrences.remove(openIssueFailedTransition));
    }


    @Override
    public List<RelatedTaskOccurrence> getRelatedTaskOccurrences() {
        return Collections.unmodifiableList(taskOccurrences);
    }


    private void createNewRelatedTaskOccurrence(TaskOccurrence taskOccurrence, String errorMessage, Instant failureTime) {
        OpenRelatedTaskOccurrenceImpl relatedTaskOccurrence = getDataModel().getInstance(OpenRelatedTaskOccurrenceImpl.class);

        if (taskOccurrence != null && errorMessage != null && failureTime != null) {
            relatedTaskOccurrence.init(this, taskOccurrence, errorMessage, failureTime);
            taskOccurrences.add(relatedTaskOccurrence);
        } else {
            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_ARGUMENT, "No task occurrence found");
        }
    }

}
