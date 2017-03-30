/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.tasks.history;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.tasks.ComTask;

import aQute.bnd.annotation.ProviderType;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ProviderType
public interface ComSessionBuilder extends BuildsStatistics<ComSessionBuilder> {

    ConnectionTask getConnectionTask();

    ComSessionBuilder connectDuration(Duration duration);

    ComSessionBuilder talkDuration(Duration duration);

    ComSessionBuilder storeDuration(Duration duration);

    ComSessionBuilder incrementFailedTasks(int numberOfFailedTasks);

    ComSessionBuilder incrementSuccessFulTasks(int numberOfSuccessFulTasks);

    ComSessionBuilder incrementNotExecutedTasks(int numberOfPlannedButNotExecutedTasks);

    ComSessionBuilder setFailedTasks(int numberOfFailedTasks);

    ComSessionBuilder setSuccessFulTasks(int numberOfSuccessFulTasks);

    ComSessionBuilder setNotExecutedTasks(int numberOfPlannedButNotExecutedTasks);

    ComSessionBuilder addJournalEntry(Instant timestamp, ComServer.LogLevel logLevel, String message);

    ComSessionBuilder addJournalEntry(Instant timestamp, ComServer.LogLevel logLevel, String message, Throwable cause);

    ComSessionBuilder addJournalEntry(Instant timestamp, ComServer.LogLevel logLevel, Throwable cause);

    void addJournalEntry(ComSessionJournalEntry entry);

    List<ComSessionJournalEntry> getJournalEntries();

    ComTaskExecutionSessionBuilder addComTaskExecutionSession(ComTaskExecution comTaskExecution, ComTask comTask, Instant startDate);

    void addComTaskExecutionSession(ComTaskExecutionSessionBuilder comTaskExecutionSessionBuilder);

    Optional<ComTaskExecutionSessionBuilder> findFor(ComTaskExecution comTaskExecution);

    List<? extends ComTaskExecutionSessionBuilder> getComTaskExecutionSessionBuilders();

    EndedComSessionBuilder endSession(Instant stopTime, ComSession.SuccessIndicator successIndicator);

    interface EndedComSessionBuilder {
        ComSession create();
    }

}