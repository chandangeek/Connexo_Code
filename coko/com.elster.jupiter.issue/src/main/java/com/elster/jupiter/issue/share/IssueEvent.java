package com.elster.jupiter.issue.share;

import java.util.Optional;

import aQute.bnd.annotation.ConsumerType;

import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.metering.EndDevice;

@ConsumerType
public interface IssueEvent {

    String getEventType();

    EndDevice getEndDevice();

    Optional<? extends Issue> findExistingIssue();

    void apply(Issue issue);

}