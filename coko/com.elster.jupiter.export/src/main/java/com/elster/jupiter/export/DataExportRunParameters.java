/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export;

import java.time.Instant;

/**
 * Created by H165696 on 10/21/2017.
 */
public interface DataExportRunParameters {
    ExportTask getTask();

    Instant getExportPeriodStart();

    Instant getExportPeriodEnd();

    Instant getUpdatePeriodStart();

    Instant getUpdatePeriodEnd();

    Instant getCreateDateTime();
}
