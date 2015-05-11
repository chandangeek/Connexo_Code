package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportStatus;
import com.elster.jupiter.tasks.TaskOccurrence;

/**
 * Copyrights EnergyICT
 * Date: 5/11/2014
 * Time: 10:22
 */
interface IDataExportOccurrence extends DataExportOccurrence {

    void persist();

    void update();

    IReadingTypeExportTask getTask();

    TaskOccurrence getTaskOccurrence();

    void end(DataExportStatus status);

    void end(DataExportStatus status, String message);
}
