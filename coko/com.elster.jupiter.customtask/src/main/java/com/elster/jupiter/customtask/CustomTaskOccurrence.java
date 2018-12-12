/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.customtask;

import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.util.logging.LogEntryFinder;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.Optional;

@ProviderType
public interface CustomTaskOccurrence {

    CustomTask getTask();

    Optional<Instant> getStartDate();

    Optional<Instant> getEndDate();

    CustomTaskStatus getStatus();

    void summarize(String summaryMessage);

    void end(CustomTaskStatus status);

    void end(CustomTaskStatus status, String message);

    void end(CustomTaskStatus status, String message, String details);

    String getStatusName();

    String getFailureReason();

    String getOccurrenceDetails();

    String getSummary();

    Instant getTriggerTime();

    boolean wasScheduled();

    Long getId();

    LogEntryFinder getLogsFinder();

    RecurrentTask getRecurrentTask();

    void update();
}
