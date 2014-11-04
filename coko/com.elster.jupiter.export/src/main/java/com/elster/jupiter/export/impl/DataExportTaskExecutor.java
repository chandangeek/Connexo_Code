package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;

/**
 * Copyrights EnergyICT
 * Date: 3/11/2014
 * Time: 9:35
 */
public class DataExportTaskExecutor implements TaskExecutor {

    private final IDataExportService service;

    public DataExportTaskExecutor(IDataExportService service) {
        this.service = service;
    }

    @Override
    public void execute(TaskOccurrence occurrence) {
        DataExportOccurrence dataExportOccurrence = service.createExportOccurrence(occurrence);
        dataExportOccurrence.save();
    }
}
