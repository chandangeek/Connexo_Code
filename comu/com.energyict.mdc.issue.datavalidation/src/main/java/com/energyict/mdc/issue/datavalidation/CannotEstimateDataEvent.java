package com.energyict.mdc.issue.datavalidation;

import java.util.Optional;

import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.metering.EndDevice;

public class CannotEstimateDataEvent implements IssueEvent {

    @Override
    public String getEventType() {
        return "";
    }

    @Override
    public EndDevice getEndDevice() {
        return null;
    }

    @Override
    public Optional<? extends Issue> findExistingIssue() {
        return Optional.empty();
    }

    @Override
    public void apply(Issue issue) {
        if (issue instanceof OpenIssueDataValidation) {
            OpenIssueDataValidation dataValidationIssue = (OpenIssueDataValidation) issue;
//            dataValidationIssue.addNotEstimatedBlock(channel, readingType, timeStamp);
        }
    }
}
