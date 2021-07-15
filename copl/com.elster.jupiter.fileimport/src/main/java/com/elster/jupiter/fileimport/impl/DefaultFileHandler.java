/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.json.JsonService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FileHandler implementation that handles files by creating a FileImport and posting a message on the fileImport queue with the FileImport id.
 */
class DefaultFileHandler implements FileHandler {

    private final ServerImportSchedule importSchedule;
    private final JsonService jsonService;
    private final TransactionService transactionService;
    private final FileImportService fileImportService;
    private final Clock clock;
    private final Logger logger = Logger.getLogger(DefaultFileHandler.class.getSimpleName());

    private static final String LOCK_FILE_EXTENSION = ".lock";

    public DefaultFileHandler(ServerImportSchedule importSchedule, JsonService jsonService, TransactionService transactionService, Clock clock, FileImportService fileImportService) {
        this.importSchedule = importSchedule;
        this.jsonService = jsonService;
        this.transactionService = transactionService;
        this.clock = clock;
        this.fileImportService = fileImportService;
    }

    @Override
    public void handle(final Path file) {
        File lockFile = file.resolveSibling(file.getFileName() + LOCK_FILE_EXTENSION).toFile();
        try (FileChannel fileChannel = new RandomAccessFile(lockFile, "rw").getChannel();) {
            if (fileChannel.tryLock() != null) {
                transactionService.run(() -> doHandle(file));
            }
        } catch (FileNotFoundException e) {
            //file handled by another appserver
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
        } finally {
            try {
                Files.delete(lockFile.toPath());
            } catch (IOException e) {
                logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
            }
        }
    }

    private void doHandle(Path file) {
        ServerFileImportOccurrence fileImportOccurrence = importSchedule.createFileImportOccurrence(file, clock);
        fileImportOccurrence.prepareProcessing();

        DestinationSpec destination = importSchedule.getDestination();

        String json = jsonService.serialize(new FileImportMessage(fileImportOccurrence, fileImportService.getAppServerName().orElse(null)));
        destination.message(json).send();
    }
}
