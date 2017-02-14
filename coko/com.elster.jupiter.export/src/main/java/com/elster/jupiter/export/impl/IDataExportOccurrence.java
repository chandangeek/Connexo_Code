/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportStatus;
import com.elster.jupiter.tasks.TaskOccurrence;

interface IDataExportOccurrence extends DataExportOccurrence {

    void persist();

    void update();

    IExportTask getTask();

    TaskOccurrence getTaskOccurrence();

    void end(DataExportStatus status);

    void end(DataExportStatus status, String message);

    void summarize(String summaryMessage);
}
