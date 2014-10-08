package com.energyict.mdc.issue.datacollection.entity;

import com.elster.jupiter.issue.share.entity.Issue;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.google.common.base.Optional;

public interface IssueDataCollection extends Issue {
    Optional<ConnectionTask> getConnectionTask();
    void setConnectionTask(ConnectionTask task);

    Optional<ComTaskExecution> getCommunicationTask();
    void setCommunicationTask(ComTaskExecution task);

    String getDeviceSerialNumber();
    void setDeviceSerialNumber(String deviceSerialNumber);
}
