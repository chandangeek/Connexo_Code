package com.energyict.mdc.device.data.tasks.history;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.tasks.ComTask;

import aQute.bnd.annotation.ProviderType;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 * Date: 28/04/2014
 * Time: 17:02
 */
@ProviderType
public interface ComSessionBuilder extends BuildsStatistics<ComSessionBuilder> {

    ConnectionTask getConnectionTask();

    ComSessionBuilder connectDuration(Duration duration);

    ComSessionBuilder talkDuration(Duration duration);

    ComSessionBuilder storeDuration(Duration duration);

    ComSessionBuilder incrementFailedTasks(int numberOfFailedTasks);

    ComSessionBuilder incrementSuccessFulTasks(int numberOfSuccessFulTasks);

    ComSessionBuilder incrementNotExecutedTasks(int numberOfPlannedButNotExecutedTasks);

    ComSessionBuilder addJournalEntry(Instant timestamp, ComServer.LogLevel logLevel, String message);

    ComSessionBuilder addJournalEntry(Instant timestamp, ComServer.LogLevel logLevel, String message, Throwable cause);

    ComSessionBuilder addJournalEntry(Instant timestamp, ComServer.LogLevel logLevel, Throwable cause);

    ComTaskExecutionSessionBuilder addComTaskExecutionSession(ComTaskExecution comTaskExecution, ComTask comTask, Device device, Instant startDate);

    Optional<ComTaskExecutionSessionBuilder> findFor(ComTaskExecution comTaskExecution);

    EndedComSessionBuilder endSession(Instant stopTime, ComSession.SuccessIndicator successIndicator);

    public interface EndedComSessionBuilder {
        ComSession create();
    }

}