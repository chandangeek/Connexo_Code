package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.*;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.json.JsonService;

import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static com.elster.jupiter.util.conditions.Operator.EQUAL;

/**
 * MessageHandler that interprets messages to contain FileImportMessages, and that consequently passes the matching FileImport instance to the configured FileImporter.
 */
class StreamImportMessageHandler implements MessageHandler {

    private final JsonService jsonService;
    private final FileImportService fileImportService;
    private final Thesaurus thesaurus;
    private final Clock clock;

    public StreamImportMessageHandler(JsonService jsonService, Thesaurus thesaurus, Clock clock,FileImportService fileImportService) {
        this.jsonService = jsonService;
        this.thesaurus = thesaurus;
        this.clock = clock;
        this.fileImportService = fileImportService;
    }

    @Override
    public void process(Message message) {
        FileImportOccurrence fileImportOccurrence = getFileImportOccurrence(message);
        if (fileImportOccurrence != null) {
            try {
                String importerName = fileImportOccurrence.getImportSchedule().getImporterName();
                Map<String, Object> propertyMap = new HashMap<>();

                FileImporterFactory fileImporterFactory = fileImportService.getImportFactory(importerName)
                        .orElseThrow(() -> new NoSuchDataImporter(thesaurus, importerName));
                List<FileImporterProperty> importerProperties = fileImportOccurrence.getImportSchedule().getImporterProperties();
                for (FileImporterProperty property : importerProperties) {
                    propertyMap.put(property.getName(), property.useDefault() ? getDefaultValue(fileImporterFactory, property) : property.getValue());
                }
                FileImporter importer = fileImporterFactory.createImporter(propertyMap);
                importer.process(fileImportOccurrence);
            } catch (Exception e) {
                fileImportOccurrence.getLogger().log(Level.SEVERE, e.getLocalizedMessage(),e);
            }
        }
    }

    private Object getDefaultValue(FileImporterFactory fileImporterFactory, FileImporterProperty property) {
        return fileImporterFactory.getPropertySpecs().stream().filter(dep -> dep.getName().equals(property.getName()))
                .findFirst().orElseThrow(IllegalArgumentException::new).getPossibleValues().getDefault();
    }

    private FileImportOccurrence getFileImportOccurrence(Message message) {
        FileImportOccurrence fileImportOccurrence = null;
        FileImportMessage fileImportMessage = getFileImportMessage(message);
        if (fileImportMessage != null) {
            fileImportOccurrence = fileImportService.getFileImportOccurrence(fileImportMessage.fileImportId).get();
        }
        return fileImportOccurrence;
    }

    private FileImportMessage getFileImportMessage(Message message) {
        return jsonService.deserialize(message.getPayload(), FileImportMessage.class);
    }
}
