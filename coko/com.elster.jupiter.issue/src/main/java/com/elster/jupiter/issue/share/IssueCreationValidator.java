package com.elster.jupiter.issue.share;

import aQute.bnd.annotation.ConsumerType;

@ConsumerType
public interface IssueCreationValidator {
    boolean isValidCreationEvent(IssueEvent issueEvent);
}
