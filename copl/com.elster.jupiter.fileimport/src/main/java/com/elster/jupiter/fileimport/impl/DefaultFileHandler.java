/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.util.json.JsonService;

import java.nio.file.Path;
import java.time.Clock;

/**
 * FileHandler implementation that handles files by creating a FileImport and posting a message on the fileImport queue with the FileImport id.
 */
class DefaultFileHandler implements FileHandler {

    private final ServerImportSchedule importSchedule;
    private final JsonService jsonService;
    private final TransactionService transactionService;
    private final Clock clock;

    public DefaultFileHandler(ServerImportSchedule importSchedule, JsonService jsonService, TransactionService transactionService, Clock clock) {
        this.importSchedule = importSchedule;
        this.jsonService = jsonService;
        this.transactionService = transactionService;
        this.clock = clock;
    }

    @Override
    public void handle(final Path file) {
        transactionService.execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                doHandle(file);
            }
        });
    }

    private void doHandle(Path file) {
        ServerFileImportOccurrence fileImportOccurrence = importSchedule.createFileImportOccurrence(file, clock);
        fileImportOccurrence.prepareProcessing();

        DestinationSpec destination = importSchedule.getDestination();

        String json = jsonService.serialize(new FileImportMessage(fileImportOccurrence));
        destination.message(json).send();
    }
}
