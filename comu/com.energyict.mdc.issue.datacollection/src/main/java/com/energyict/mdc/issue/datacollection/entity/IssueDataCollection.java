package com.energyict.mdc.issue.datacollection.entity;

import com.elster.jupiter.issue.share.entity.Issue;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSession;

import java.time.Instant;
import java.util.Optional;

public interface IssueDataCollection extends Issue {

    Optional<ConnectionTask> getConnectionTask();
    void setConnectionTask(ConnectionTask task);

    Optional<ComTaskExecution> getCommunicationTask();
    void setCommunicationTask(ComTaskExecution task);

    String getDeviceMRID();
    void setDeviceMRID(String deviceMRID);
    
    Optional<ComSession> getComSession();
    void setComSession(ComSession comSession);

    Instant getFirstConnectionAttempt();
    void setFirstConnectionAttempt(Instant firstConnectionAttempt);

    Instant getLastConnectionAttempt();
    void setLastConnectionAttempt(Instant lastConnectionAttempt);

    long getConnectionAttemptsNumber();
    void setConnectionAttemptsNumber(long connectionAttemptsNumber);
    void incrementConnectionAttemptsNumber();
}