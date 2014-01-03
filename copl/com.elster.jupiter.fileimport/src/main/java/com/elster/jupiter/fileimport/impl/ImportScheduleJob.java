package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.Predicates;
import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.json.JsonService;

/**
 * CronJob to scan the import directory of the configured ImportSchedule, and pass files to the DefaultFileHandler.
 */
class ImportScheduleJob implements CronJob {

    private final ImportSchedule importSchedule;
    private final FolderScanningJob folderScanningJob;

    public ImportScheduleJob(Predicates predicates, FileSystem fileSystem, JsonService jsonService, ImportSchedule importSchedule, TransactionService transactionService) {
        this.importSchedule = importSchedule;
        folderScanningJob = new FolderScanningJob(new PollingFolderScanner(predicates, fileSystem, importSchedule.getImportDirectory().toPath()), new DefaultFileHandler(importSchedule, jsonService, transactionService));
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
