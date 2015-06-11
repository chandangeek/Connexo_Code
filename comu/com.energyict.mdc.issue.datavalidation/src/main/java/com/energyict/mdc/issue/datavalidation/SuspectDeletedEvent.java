package com.energyict.mdc.issue.datavalidation;

import java.util.Optional;

import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.metering.EndDevice;

public class SuspectDeletedEvent implements IssueEvent {

    @Override
    public String getEventType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EndDevice getEndDevice() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<? extends Issue> findExistingIssue() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void apply(Issue issue) {
        // TODO Auto-generated method stub

    }

}
