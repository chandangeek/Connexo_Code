package com.elster.jupiter.issue;

import com.google.common.base.CaseFormat;

/**
 This enumeration provides list of available statuses for issue type
 */
public enum IssueStatus {
    OPEN,
    CLOSED,
    POSTPONED,
    IN_PROGRESS,
    REJECTED;

    public String toString() {
        return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.name());

    }
}
