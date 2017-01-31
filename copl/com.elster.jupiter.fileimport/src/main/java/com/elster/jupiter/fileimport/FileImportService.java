/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.properties.PropertySpec;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Main FIM service that allows managing schedules for FileImports
 */
@ProviderType
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
     *
     * @param importSchedule
     */
    void schedule(ImportSchedule importSchedule);

    /**
     * Cancel job execution of the given importSchedule.
     *
     * @param importSchedule
     */
    void unschedule(ImportSchedule importSchedule);

    void unscheduleAll();

    /**
     * @param id
     * @return the ImportSchedule with the given id, optionally, as it may not exist
     */
    Optional<ImportSchedule> getImportSchedule(long id);

    Optional<ImportSchedule> findAndLockImportScheduleByIdAndVersion(long id, long version);

    /**
     * @param importerName
     * @return the FileImporterFactory with the given name,  optionally, as it may not exist
     */
    Optional<FileImporterFactory> getImportFactory(String importerName);

    /**
     * @param applicationName the name of the application to look for applicable FileImporterFactory
     * @return the list of importers for the given applicationName
     */
    List<FileImporterFactory> getAvailableImporters(String applicationName);

    /**
     * @return query to the import schedules
     */
    Query<ImportSchedule> getImportSchedulesQuery();

    /**
     * @return the list of import schedules
     */
    Finder<ImportSchedule> findImportSchedules(String applicationName);

    /**
     * @return the list of import schedules including obsolete ones
     */
    Finder<ImportSchedule> findAllImportSchedules(String applicationName);

    /**
     * @return the a file occurrence finder for specified application name and/or schedule id
     */
    FileImportOccurrenceFinderBuilder getFileImportOccurrenceFinderBuilder(String applicationName, Long importScheduleId);

    /**
     * @return the file occurrece with specified id
     */
    Optional<FileImportOccurrence> getFileImportOccurrence(Long id);

    /**
     * @param importerName
     * @return the List of importer property specs
     */
    List<PropertySpec> getPropertiesSpecsForImporter(String importerName);

    /**
     * @return the list of import schedules
     */
    List<ImportSchedule> getImportSchedules();

    /**
     * @return the import schedule with specified name
     */
    Optional<ImportSchedule> getImportSchedule(String name);

    /**
     * set the app server base path
     */
    void setBasePath(Path basePath);

    /**
     * @return the base path of the running app server import folder
     */
    Path getBasePath();


}
