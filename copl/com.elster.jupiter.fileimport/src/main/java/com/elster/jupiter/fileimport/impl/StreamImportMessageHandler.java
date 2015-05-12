package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.*;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.json.JsonService;

import java.util.HashMap;
import java.util.List;
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
            Map<String, Object> propertyMap = new HashMap<>();

            FileImporterFactory fileImporterFactory =  fileImportService.getImportFactory(importerName)
                    .orElseThrow(() -> new NoSuchDataImporter(thesaurus, importerName));
            List<FileImporterProperty> importerProperties = fileImport.getImportSchedule().getImporterProperties();
            for (FileImporterProperty property : importerProperties) {
                propertyMap.put(property.getName(), property.useDefault() ? getDefaultValue(fileImporterFactory, property) : property.getValue());
            }
            FileImporter importer =fileImporterFactory.createImporter(propertyMap);
            importer.process(fileImport);
        }
    }

    private Object getDefaultValue(FileImporterFactory fileImporterFactory, FileImporterProperty property) {
        return fileImporterFactory.getProperties().stream().filter(dep -> dep.getName().equals(property.getName()))
                .findFirst().orElseThrow(IllegalArgumentException::new).getPossibleValues().getDefault();
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
