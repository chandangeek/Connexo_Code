package com.elster.jupiter.fileimport;

import com.elster.jupiter.messaging.subscriber.MessageHandler;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Main FIM service that allows managing schedules for FileImports
 */
public interface FileImportService {

    String COMPONENT_NAME = "FIM";

    /**
     * @return a builder for ImportSchedules
     */
    ImportScheduleBuilder newBuilder();

    /**
     * @return a MessageHandler configured to delegate file import messages to file importers
     */
    MessageHandler createMessageHandler();

    /**
     * Submits the given importSchedule for executing according to its schedule.
     * @param importSchedule
     */
    void schedule(ImportSchedule importSchedule);

    /**
     * @param id
     * @return the ImportSchedule with the given id, optionally, as it may not exist
     */
    Optional<ImportSchedule> getImportSchedule(long id);

    List<ImportSchedule> getImportSchedules();

    /**
     * @param importerName
     * @return the FileImporterFactory with the given name,  optionally, as it may not exist
     */
    Optional<FileImporterFactory> getImportFactory(String importerName);

    public List<String> getAvailableImporters();
}
