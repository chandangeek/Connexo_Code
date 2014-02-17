package com.elster.jupiter.issue;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.util.time.UtcInstant;

public interface Issue {

    long getId();

    IssueStatus getStatus();

    String getReason();

    UtcInstant getDueDate();

    EndDevice getDevice();

    long getVersion();

    IssueAssignee getAssignee();
}
