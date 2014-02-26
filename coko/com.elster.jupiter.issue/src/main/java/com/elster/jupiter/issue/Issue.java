package com.elster.jupiter.issue;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.util.time.UtcInstant;

public interface Issue {
    String TYPE_IDENTIFIER = "C";

    long getId();

    IssueStatus getStatus();

    String getTitle();

    IssueReason getReason();

    UtcInstant getDueDate();

    EndDevice getDevice();

    Meter getMeter();

    long getVersion();

    IssueAssignee getAssignee();

    UtcInstant getCreateTime();
}
