package com.elster.jupiter.fileimport;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
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
     * @param fileImporter
     * @return a MessageHandler configured to delegate file import messages to the given FileImporter.
     */
    MessageHandler createMessageHandler(FileImporter fileImporter);

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
}
