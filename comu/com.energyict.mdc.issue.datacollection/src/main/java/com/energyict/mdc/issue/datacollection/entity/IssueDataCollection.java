/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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

    Instant getFirstConnectionAttemptTimestamp();
    void setFirstConnectionAttemptTimestamp(Instant firstConnectionAttempt);

    Instant getLastConnectionAttemptTimestamp();
    void setLastConnectionAttemptTimestamp(Instant lastConnectionAttempt);

    long getConnectionAttempt();
    void setConnectionAttempt(long connectionAttemptsNumber);
    void incrementConnectionAttempt();
}