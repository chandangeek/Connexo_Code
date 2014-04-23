package com.elster.jupiter.issue.share.cep;

import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.metering.EndDevice;

public interface IssueEvent {
    String getEventType();
    IssueStatus getStatus();
    EndDevice getDevice();
}
