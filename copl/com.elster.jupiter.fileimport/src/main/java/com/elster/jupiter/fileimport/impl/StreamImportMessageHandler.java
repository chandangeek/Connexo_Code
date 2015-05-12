package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.fileimport.NoSuchDataImporter;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.json.JsonService;

import java.util.HashMap;
import java.util.Map;

/**
 * MessageHandler that interprets messages to contain FileImportMessages, and that consequently passes the matching FileImport instance to the configured FileImporter.
 */
class StreamImportMessageHandler implements MessageHandler {

    private final DataModel dataModel;
    private final JsonService jsonService;
    private final FileImportService fileImportService;
    private final Thesaurus thesaurus;

    public StreamImportMessageHandler(DataModel dataModel, JsonService jsonService, Thesaurus thesaurus, FileImportService fileImportService) {
        this.dataModel = dataModel;
        this.jsonService = jsonService;
        this.thesaurus = thesaurus;
        this.fileImportService = fileImportService;
    }


    @Override
    public void process(Message message) {
        FileImportImpl fileImport = getFileImport(message);
        if (fileImport != null) {
            String importerName = fileImport.getImportSchedule().getImporterName();
            Map<String, Object> properties = new HashMap<>();
            FileImporter importer = fileImportService.getImportFactory(importerName)
                .orElseThrow(() -> new NoSuchDataImporter(thesaurus, importerName)).createImporter(properties);

            importer.process(fileImport);
        }
    }

    private FileImportImpl getFileImport(Message message) {
        FileImportImpl fileImport = null;
        FileImportMessage fileImportMessage = getFileImportMessage(message);
        if (fileImportMessage != null) {
            fileImport = dataModel.mapper(FileImportImpl.class).getOptional(fileImportMessage.fileImportId).get();
        }
        return fileImport;
    }

    private FileImportMessage getFileImportMessage(Message message) {
        return jsonService.deserialize(message.getPayload(), FileImportMessage.class);
    }
}
