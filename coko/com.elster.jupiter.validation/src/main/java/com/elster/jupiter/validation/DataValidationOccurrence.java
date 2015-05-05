package com.elster.jupiter.validation;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.util.logging.LogEntry;
import com.elster.jupiter.util.logging.LogEntryFinder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;


@ProviderType
public interface DataValidationOccurrence {

    public DataValidationTask getTask();

    Optional<Instant> getStartDate();

    String getFailureReason();

    Optional<Instant> getEndDate();

    DataValidationTaskStatus getStatus();

    Instant getTriggerTime();

    boolean wasScheduled();

    Long getId();

    List<? extends LogEntry> getLogs();

    LogEntryFinder getLogsFinder();

    void persist();

    void update();

    TaskOccurrence getTaskOccurrence();

    public void end(DataValidationTaskStatus status, String message);

    public void end(DataValidationTaskStatus status);

}
