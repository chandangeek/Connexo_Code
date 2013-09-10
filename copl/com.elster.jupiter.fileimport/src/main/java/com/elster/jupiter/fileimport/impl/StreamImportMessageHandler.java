package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImport;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;

/**
 * MessageHandler that interprets messages to contain FileImportMessages, and that consequently passes the matching FileImport instance to the configured FileImporter.
 */
class StreamImportMessageHandler implements MessageHandler {

    private final FileImporter streamImporter;

    public StreamImportMessageHandler(FileImporter streamImporter) {
        this.streamImporter = streamImporter;
    }

    @Override
    public void process(Message message) {
        FileImport fileImport = getFileImport(message);
        if (fileImport != null) {
            streamImporter.process(fileImport);
        }
    }

    private FileImport getFileImport(Message message) {
        FileImport fileImport = null;
        FileImportMessage fileImportMessage = getFileImportMessage(message);
        if (fileImportMessage != null) {
            fileImport = Bus.getOrmClient().getFileImportFactory().get(fileImportMessage.fileImportId).get();
        }
        return fileImport;
    }

    private FileImportMessage getFileImportMessage(Message message) {
        return Bus.getJsonService().deserialize(message.getPayload(), FileImportMessage.class);
    }
}
