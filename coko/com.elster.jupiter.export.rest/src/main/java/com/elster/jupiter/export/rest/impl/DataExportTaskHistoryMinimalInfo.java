/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.rest.impl;

import java.time.Instant;

public class DataExportTaskHistoryMinimalInfo {

    public Long id;
    public String trigger;
    public Instant startedOn;
    public Instant finishedOn;
    public Long duration;
    public String status;
    public String reason;
    public Instant lastRun;
    public Instant exportPeriodFrom;
    public Instant exportPeriodTo;
    public Instant statusDate;
    public String statusPrefix;

}