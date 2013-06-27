package com.elster.jupiter.fileimport;

import com.elster.jupiter.messaging.consumer.MessageHandler;

public interface FileImportService {

    ImportScheduleBuilder newBuilder();

    MessageHandler createMessageHandler(FileImporter fileImporter);
}
