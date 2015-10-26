package com.elster.jupiter.issue.share;

import aQute.bnd.annotation.ConsumerType;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.metering.EndDevice;

import java.util.Optional;

@ConsumerType
public interface IssueEvent {

    String getEventType();

    EndDevice getEndDevice();

    Optional<? extends OpenIssue> findExistingIssue();

    void apply(Issue issue);

}