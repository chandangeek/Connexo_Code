package com.elster.jupiter.estimation;

import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.util.logging.LogEntry;
import com.elster.jupiter.util.logging.LogEntryFinder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;


public interface EstimationTaskOccurrence {

    EstimationTask getTask();

    Optional<Instant> getStartDate();

    String getFailureReason();

    Optional<Instant> getEndDate();

    EstimationTaskStatus getStatus();

    Instant getTriggerTime();

    boolean wasScheduled();

    Long getId();

    List<? extends LogEntry> getLogs();

    LogEntryFinder getLogsFinder();

    void persist();

    void update();

    TaskOccurrence getTaskOccurrence();

    void end(EstimationTaskStatus status, String message);

    void end(EstimationTaskStatus status);
}
