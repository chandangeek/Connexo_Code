package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.Predicates;
import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.json.JsonService;

import javax.inject.Inject;

/**
 * CronJob to scan the import directory of the configured ImportSchedule, and pass files to the DefaultFileHandler.
 */
class ImportScheduleJob implements CronJob {

    private final ImportSchedule importSchedule;
    private final FolderScanningJob folderScanningJob;
    private final Thesaurus thesaurus;

    @Inject
    public ImportScheduleJob(Predicates predicates, FileSystem fileSystem, JsonService jsonService, ImportSchedule importSchedule, TransactionService transactionService, Thesaurus thesaurus) {
        this.importSchedule = importSchedule;
        this.thesaurus = thesaurus;
        folderScanningJob = new FolderScanningJob(new PollingFolderScanner(predicates, fileSystem, importSchedule.getImportDirectory().toPath(), this.thesaurus), new DefaultFileHandler(importSchedule, jsonService, transactionService));
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
