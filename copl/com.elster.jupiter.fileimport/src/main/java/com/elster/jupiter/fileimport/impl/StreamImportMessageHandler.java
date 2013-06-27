package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImport;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.messaging.consumer.MessageHandler;
import oracle.jdbc.aq.AQMessage;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.sql.SQLException;

public class StreamImportMessageHandler implements MessageHandler {

    private final FileImporter streamImporter;

    public StreamImportMessageHandler(FileImporter streamImporter) {
        this.streamImporter = streamImporter;
    }

    @Override
    public void process(AQMessage message) throws SQLException {
        FileImport fileImport = getFileImport(message);
        if (fileImport != null) {
            streamImporter.process(fileImport);
        }
    }

    private FileImport getFileImport(AQMessage message) throws SQLException {
        FileImport fileImport = null;
        FileImportMessage fileImportMessage = getFileImportMessage(message);
        if (fileImportMessage != null) {
            fileImport = Bus.getOrmClient().getFileImportFactory().get(fileImportMessage.fileImportId).get();
        }
        return fileImport;
    }

    private FileImportMessage getFileImportMessage(AQMessage message) throws SQLException {
        try {
            return new ObjectMapper().readValue(message.getPayload(), FileImportMessage.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
