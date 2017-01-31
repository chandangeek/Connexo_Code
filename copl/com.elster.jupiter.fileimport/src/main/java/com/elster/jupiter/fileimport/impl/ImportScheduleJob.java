/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.ScheduleExpression;

import javax.inject.Inject;
import java.nio.file.Path;
import java.time.Clock;
import java.util.function.Predicate;

/**
 * CronJob to scan the import directory of the configured ImportSchedule, and pass files to the DefaultFileHandler.
 */
class ImportScheduleJob implements CronJob {

    private final TransactionService transactionService;
    private final FileUtils fileSystem;
    private final Clock clock;
    private final JsonService jsonService;
    private final Thesaurus thesaurus;
    private final FileImportService fileImportService;
    private final Predicate<Path> filter;
    private final Long importScheduleId;

    @Inject
    public ImportScheduleJob(Predicate<Path> filter, FileUtils fileSystem, JsonService jsonService, FileImportService fileImportService, Long importScheduleId, TransactionService transactionService, Thesaurus thesaurus, CronExpressionParser cronExpressionParser, Clock clock) {
        this.filter = filter;
        this.fileSystem = fileSystem;
        this.jsonService = jsonService;
        this.clock = clock;
        this.transactionService = transactionService;
        this.fileImportService = fileImportService;
        this.importScheduleId = importScheduleId;
        this.thesaurus = thesaurus;

    }

    @Override
    public Long getId() {
        return importScheduleId;
    }

    @Override
    public ScheduleExpression getSchedule() {
        return fileImportService.getImportSchedule(importScheduleId).orElseThrow(IllegalArgumentException::new).getScheduleExpression();
    }

    @Override
    public void run() {
        fileImportService.getImportSchedule(importScheduleId)
                .filter(ImportSchedule::isActive)
                .map(ServerImportSchedule.class::cast)
                .ifPresent(importSchedule -> {
                    Path importFolder = fileImportService.getBasePath().resolve(importSchedule.getImportDirectory());
                    FolderScanningJob folderScanningJob = new FolderScanningJob(
                            new PollingFolderScanner(filter, fileSystem, importFolder, importSchedule.getPathMatcher(), this.thesaurus),
                            new DefaultFileHandler(importSchedule, jsonService, transactionService, clock));
                    folderScanningJob.run();

                });

    }
}
