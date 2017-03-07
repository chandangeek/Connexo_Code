/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.orm.HasAuditInfo;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.util.HasName;
import com.elster.jupiter.util.time.ScheduleExpression;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;


/**
 * Models a task that should put a message on the task queue recurrently.
 */
@ProviderType
public interface RecurrentTask extends HasName, HasAuditInfo {

    long getId();

    void updateNextExecution();

    DestinationSpec getDestination();

    String getPayLoad();

    Instant getNextExecution();

    Optional<Instant> getLastRun();

    void save();

    void delete();

    void suspend();

    void resume();

    ScheduleExpression getScheduleExpression();

    void setNextExecution(Instant instant);

    void setScheduleExpression(ScheduleExpression scheduleExpression);

    void triggerNow();
    
    /*
     * Note that runNow has different transactional behavior as triggerNow or a normal scheduled execution
     * When using runNow both execute and postExecute will be executed as part of the caller's transaction.
     */
    TaskOccurrence runNow(TaskExecutor executor);

    Optional<TaskOccurrence> getLastOccurrence();

    List<TaskOccurrence> getTaskOccurrences();

    /**
     * @since v1.1
     */
    History<? extends RecurrentTask> getHistory();

    String getApplication();

    void setName(String name);

    /**
     * @since v2.0
     */
    Optional<RecurrentTask> getVersionAt(Instant time);

    void setLogLevel(int level);

    int getLogLevel();
}
