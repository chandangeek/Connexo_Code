package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.util.cron.CronExpression;

public class ImportScheduleJob implements CronJob {

    private final ImportSchedule importSchedule;
    private final FolderScanningJob folderScanningJob;

    public ImportScheduleJob(ImportSchedule importSchedule) {
        this.importSchedule = importSchedule;
        folderScanningJob = new FolderScanningJob(new PollingFolderScanner(importSchedule.getImportDirectory().toPath()), new DefaultFileHandler());
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
