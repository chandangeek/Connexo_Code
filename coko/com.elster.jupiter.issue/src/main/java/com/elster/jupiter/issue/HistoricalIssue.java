package com.elster.jupiter.issue;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.util.time.UtcInstant;

/**
 * The same methods as for Issue interface (copy paste due to ORM retrictions)
 */
public interface HistoricalIssue {

    long getId();

    IssueStatus getStatus();

    String getTitle();

    IssueReason getReason();

    UtcInstant getDueDate();

    EndDevice getDevice();

    long getVersion();

    IssueAssignee getAssignee();

    UtcInstant getCreateTime();
}
