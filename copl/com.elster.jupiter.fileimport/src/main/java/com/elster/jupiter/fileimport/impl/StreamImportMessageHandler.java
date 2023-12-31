/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.fileimport.FileImporterFactory;
import com.elster.jupiter.fileimport.FileImporterProperty;
import com.elster.jupiter.fileimport.NoSuchDataImporter;
import com.elster.jupiter.fileimport.Status;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.exception.BaseException;
import com.elster.jupiter.util.json.JsonService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * MessageHandler that interprets messages to contain FileImportMessages, and that consequently passes the matching FileImport instance to the configured FileImporter.
 */
class StreamImportMessageHandler implements MessageHandler {
    private final JsonService jsonService;
    private final FileImportService fileImportService;
    private final TransactionService transactionService;
    private final Thesaurus thesaurus;

    private transient ServerFileImportOccurrence fileImportOccurrence;

    public StreamImportMessageHandler(JsonService jsonService, Thesaurus thesaurus, FileImportService fileImportService, TransactionService transactionService) {
        this.jsonService = jsonService;
        this.thesaurus = thesaurus;
        this.fileImportService = fileImportService;
        this.transactionService = transactionService;
    }

    @Override
    public boolean validate(Message message) {
        FileImportMessage fileImportMessage = getFileImportMessage(message);
        return fileImportService.getAppServerName().filter(name -> name.equals(fileImportMessage.appServerName)).isPresent();
    }

    @Override
    public void process(Message message) {
        ServerFileImportOccurrence fileImportOccurrence = getFileImportOccurrence(message);
        if (fileImportOccurrence != null) {
            try {
                String importerName = fileImportOccurrence.getImportSchedule().getImporterName();
                FileImporterFactory fileImporterFactory = getFileImporterFactory(importerName);

                if (fileImporterFactory.requiresTransaction()) {
                    // execute in this transaction
                    FileImporter importer = createFileImporter(fileImportOccurrence, fileImporterFactory);
                    importer.process(fileImportOccurrence);
                } else {
                    // save the occurrence for post-processing without transaction
                    this.fileImportOccurrence = fileImportOccurrence;
                }
            } catch (Exception e) {
                handleException(fileImportOccurrence, e);
            } finally {
                fileImportOccurrence.save();
            }
        }
    }

    @Override
    public void onMessageDelete(Message message) {
        if (fileImportOccurrence != null) {
            try {
                FileImporterFactory fileImporterFactory = getFileImporterFactory(fileImportOccurrence.getImportSchedule().getImporterName());
                FileImporter importer = createFileImporter(fileImportOccurrence, fileImporterFactory);
                importer.process(new TransactionWrappedFileImportOccurenceImpl(transactionService, fileImportOccurrence));
            } catch (Exception ex) {
                transactionService.run(() -> handleException(fileImportOccurrence, ex));
            } finally {
                transactionService.run(() -> fileImportOccurrence.save());
                fileImportOccurrence = null;
            }
        }
    }

    private FileImporter createFileImporter(FileImportOccurrence fileImportOccurrence, FileImporterFactory fileImporterFactory) {
        List<FileImporterProperty> importerProperties = fileImportOccurrence.getImportSchedule().getImporterProperties();
        Map<String, Object> propertyMap = new HashMap<>();
        for (FileImporterProperty property : importerProperties) {
            propertyMap.put(property.getName(), property.useDefault() ? getDefaultValue(fileImporterFactory, property) : property.getValue());
        }
        return fileImporterFactory.createImporter(propertyMap);
    }

    private void handleException(ServerFileImportOccurrence occurrence, Exception ex) {
        String message = ex.getLocalizedMessage();
        if (BaseException.class.isAssignableFrom(ex.getClass())) {
            message = "(" + ((BaseException) ex).getErrorCode() + ") " + message;
        }
        occurrence.getLogger().log(Level.SEVERE, message, ex);
        if (Status.PROCESSING.equals(occurrence.getStatus())) {
            occurrence.markFailure(ex.getLocalizedMessage());
        }
    }

    private FileImporterFactory getFileImporterFactory(String importerName) {
        return fileImportService.getImportFactory(importerName)
                .orElseThrow(() -> new NoSuchDataImporter(thesaurus, importerName));
    }

    private Object getDefaultValue(FileImporterFactory fileImporterFactory, FileImporterProperty property) {
        return fileImporterFactory.getPropertySpecs().stream().filter(dep -> dep.getName().equals(property.getName()))
                .findFirst().orElseThrow(IllegalArgumentException::new).getPossibleValues().getDefault();
    }

    private ServerFileImportOccurrence getFileImportOccurrence(Message message) {
        FileImportOccurrence fileImportOccurrence = null;
        FileImportMessage fileImportMessage = getFileImportMessage(message);
        if (fileImportMessage != null) {
            fileImportOccurrence = fileImportService.getFileImportOccurrence(fileImportMessage.fileImportId).orElse(null);
        }
        return (ServerFileImportOccurrence) fileImportOccurrence;
    }

    private FileImportMessage getFileImportMessage(Message message) {
        return jsonService.deserialize(message.getPayload(), FileImportMessage.class);
    }
}
