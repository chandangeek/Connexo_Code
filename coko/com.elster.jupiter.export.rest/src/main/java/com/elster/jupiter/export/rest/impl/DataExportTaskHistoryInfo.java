/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.rest.impl;

import java.time.Instant;

public class DataExportTaskHistoryInfo extends DataExportTaskHistoryMinimalInfo {

    public Instant updatePeriodFrom;
    public Instant updatePeriodTo;
    public DataExportTaskInfo task;
    public String summary;

}