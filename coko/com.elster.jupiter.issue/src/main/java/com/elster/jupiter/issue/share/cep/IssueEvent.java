package com.elster.jupiter.issue.share.cep;

import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.metering.EndDevice;
import java.util.Optional;

public interface IssueEvent {
    String getEventType();
    IssueStatus getStatus();
    EndDevice getKoreDevice();

    Optional<? extends Issue> findExistingIssue();
    void apply(Issue issue);
}