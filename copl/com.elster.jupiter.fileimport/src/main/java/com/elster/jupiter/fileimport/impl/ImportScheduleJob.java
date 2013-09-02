package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.util.cron.CronExpression;

/**
 * CronJob to scan the import directory of the configured ImportSchedule, and pass files to the DefaultFileHandler.
 */
public class ImportScheduleJob implements CronJob {

    private final ImportSchedule importSchedule;
    private final FolderScanningJob folderScanningJob;

    public ImportScheduleJob(ImportSchedule importSchedule) {
        this.importSchedule = importSchedule;
        folderScanningJob = new FolderScanningJob(new PollingFolderScanner(importSchedule.getImportDirectory().toPath()), new DefaultFileHandler(importSchedule));
    }

    @Override
    public CronExpression getSchedule() {
        return importSchedule.getScheduleExpression();
    }

    @Override
    public void run() {
        folderScanningJob.run();
    }
}
