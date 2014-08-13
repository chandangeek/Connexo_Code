package com.energyict.mdc.device.data.tasks.history;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.google.common.base.Optional;
import org.joda.time.Duration;

import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 28/04/2014
 * Time: 17:02
 */
public interface ComSessionBuilder extends BuildsStatistics<ComSessionBuilder> {

    ComSessionBuilder totalTime(long totalTime);

    ComSessionBuilder connectDuration(Duration duration);

    ComSessionBuilder talkDuration(Duration duration);

    ComSessionBuilder storeDuration(Duration duration);

    ComSessionBuilder successful();

    ComSessionBuilder unsuccessful();

    ComSessionBuilder incrementSuccessFulTasks();

    ComSessionBuilder incrementFailedTasks();

    ComSessionBuilder incrementNotExecutedTasks();

    ComSessionBuilder incrementFailedTasks(int numberOfFailedTasks);

    ComSessionBuilder incrementSuccessFulTasks(int numberOfSuccessFulTasks);

    ComSessionBuilder incrementNotExecutedTasks(int numberOfPlannedButNotExecutedTasks);

    ComSessionBuilder addJournalEntry(Date timestamp, String message, Throwable cause);

    ComTaskExecutionSessionBuilder addComTaskExecutionSession(ComTaskExecution comTaskExecution, Device device, Date startDate);

    Optional<ComTaskExecutionSessionBuilder> findFor(ComTaskExecution comTaskExecution);

    EndedComSessionBuilder endSession(Date stopTime, ComSession.SuccessIndicator successIndicator);

    public interface EndedComSessionBuilder {

        ComSession create();
    }
}
