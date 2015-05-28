package com.elster.jupiter.fileimport.impl;

import java.nio.file.Path;
import java.util.function.Predicate;

import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.ScheduleExpression;

import javax.inject.Inject;

/**
 * CronJob to scan the import directory of the configured ImportSchedule, and pass files to the DefaultFileHandler.
 */
class ImportScheduleJob implements CronJob {

    private final ImportSchedule importSchedule;
    private final FolderScanningJob folderScanningJob;
    private final CronExpressionParser cronExpressionParser;
    private final Thesaurus thesaurus;

    @Inject
    public ImportScheduleJob(Predicate<Path> filter, FileSystem fileSystem, JsonService jsonService, ImportSchedule importSchedule, TransactionService transactionService, Thesaurus thesaurus, CronExpressionParser cronExpressionParser) {
        this.importSchedule = importSchedule;
        this.thesaurus = thesaurus;
        this.cronExpressionParser = cronExpressionParser;
        folderScanningJob = new FolderScanningJob(
                new PollingFolderScanner(filter, fileSystem, importSchedule.getImportDirectory().toPath(), importSchedule.getPathMatcher(), this.thesaurus),
                new DefaultFileHandler(importSchedule, jsonService, transactionService));
    }

    @Override
    public ScheduleExpression getSchedule() {
        ScheduleExpression scheduleExpression = importSchedule.getScheduleExpression();
        return scheduleExpression;

    }

    @Override
    public void run() {
        folderScanningJob.run();
    }
}
