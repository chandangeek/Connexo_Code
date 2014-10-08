package com.elster.jupiter.issue.share.cep;

import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.metering.EndDevice;
import com.google.common.base.Optional;

public interface IssueEvent {
    String getEventType();
    IssueStatus getStatus();
    EndDevice getKoreDevice();

    Optional<? extends Issue> findExistingIssue(Issue baseIssue);
    void apply(Issue issue);
}