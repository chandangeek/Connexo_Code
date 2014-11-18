package com.elster.jupiter.export.impl;

import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskService;

import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 3/11/2014
 * Time: 9:35
 */
class DataExportTaskExecutor implements TaskExecutor {

    private final IDataExportService dataExportService;
    private final TaskService taskService;


    public DataExportTaskExecutor(IDataExportService dataExportService, TaskService taskService) {
        this.dataExportService = dataExportService;
        this.taskService = taskService;
    }

    @Override
    public void execute(TaskOccurrence occurrence) {
        IDataExportOccurrence dataExportOccurrence = createOccurrence(occurrence);
        dataExportOccurrence.getTask().execute(dataExportOccurrence, getLogger(occurrence));
    }

    private Logger getLogger(TaskOccurrence occurrence) {
        Logger logger = Logger.getAnonymousLogger();
        logger.addHandler(occurrence.createTaskLogHandler().asHandler());
        return logger;
    }

    private IDataExportOccurrence createOccurrence(TaskOccurrence occurrence) {
        IDataExportOccurrence dataExportOccurrence = dataExportService.createExportOccurrence(occurrence);
        dataExportOccurrence.persist();
        return dataExportOccurrence;
    }
}
