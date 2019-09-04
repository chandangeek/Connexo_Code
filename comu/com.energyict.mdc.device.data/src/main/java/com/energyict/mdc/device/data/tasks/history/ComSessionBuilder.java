/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.tasks.history;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComPortPool;
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

    void setConnectionTask(ConnectionTask connectionTask);

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

    void injectServices (DataModel dataModel, ConnectionTaskService connectionTaskService, Thesaurus thesaurus);

    EndedComSessionBuilder endSession(Instant stopTime, ComSession.SuccessIndicator successIndicator);

    interface EndedComSessionBuilder {
        ComSession create();
    }

}