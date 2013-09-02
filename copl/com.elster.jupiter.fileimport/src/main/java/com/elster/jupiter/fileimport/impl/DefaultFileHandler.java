package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImport;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.transaction.VoidTransaction;

import java.io.File;

/**
 * FileHandler implementation that handles files by creating a FileImport and posting a message on the fileImport queue with the FileImport id.
 */
public class DefaultFileHandler implements FileHandler {

    private final ImportSchedule importSchedule;

    public DefaultFileHandler(ImportSchedule importSchedule) {
        this.importSchedule = importSchedule;
    }

    @Override
    public void handle(final File file) {
        Bus.getTransactionService().execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                doHandle(file);
            }
        });
    }

    private void doHandle(File file) {
        FileImport fileImport = importSchedule.createFileImport(file);
        DestinationSpec destination = importSchedule.getDestination();

        String json = Bus.getJsonService().serialize(new FileImportMessage(fileImport));
        destination.message(json).send();
    }
}
