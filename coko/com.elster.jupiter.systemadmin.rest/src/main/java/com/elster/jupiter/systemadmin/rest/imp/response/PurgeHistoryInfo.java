/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.systemadmin.rest.imp.response;

import com.elster.jupiter.tasks.TaskOccurrence;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

public class PurgeHistoryInfo {
    public long id;
    public Long startDate;
    public String status;
    public Long duration;

    public PurgeHistoryInfo(){}

    public PurgeHistoryInfo(TaskOccurrence occurrence, Clock clock) {
        if (occurrence != null) {
            this.id = occurrence.getId();
            occurrence.getStartDate().ifPresent(sd -> {
                this.startDate = sd.toEpochMilli();
                this.duration = Duration.between(sd, occurrence.getEndDate().orElse(Instant.now(clock))).toMillis();
            });
            this.status = occurrence.getStatus().toString();
        }
    }
}
