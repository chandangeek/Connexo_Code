package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImport;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.util.json.JsonService;

import java.io.File;

/**
 * FileHandler implementation that handles files by creating a FileImport and posting a message on the fileImport queue with the FileImport id.
 */
class DefaultFileHandler implements FileHandler {

    private final ImportSchedule importSchedule;
    private final JsonService jsonService;
    private final TransactionService transactionService;

    public DefaultFileHandler(ImportSchedule importSchedule, JsonService jsonService, TransactionService transactionService) {
        this.importSchedule = importSchedule;
        this.jsonService = jsonService;
        this.transactionService = transactionService;
    }

    @Override
    public void handle(final File file) {
        transactionService.execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                doHandle(file);
            }
        });
    }

    private void doHandle(File file) {
        FileImport fileImport = importSchedule.createFileImport(file);
        fileImport.prepareProcessing();
        DestinationSpec destination = importSchedule.getDestination();

        String json = jsonService.serialize(new FileImportMessage(fileImport));
        destination.message(json).send();
    }
}
