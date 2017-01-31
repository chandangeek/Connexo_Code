/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.time.ScheduleExpression;

import aQute.bnd.annotation.ProviderType;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Entity that models when a certain directory should be scanned for files to import
 * and where to move them during different stages of the import cycle.
 */
@ProviderType
public interface ImportSchedule {

    /**
     * @return this entity's id
     */
    long getId();

    long getVersion();

    /**
     * @return import schedule status
     */
    boolean isActive();

    void setActive(Boolean active);

    /**
     * @return import schedule name
     */
    String getName();

    void setName(String name);

    /**
     * @return the Destination on which to post a message when a file needs processing.
     */
    DestinationSpec getDestination();

    void setDestination(String destinationName);

    /**
     * @return Path representing the directory to scan for new files to import
     */
    Path getImportDirectory();

    void setImportDirectory(Path path);

    /**
     * @return String representing the pattern to scan for new files to import
     */
    String getPathMatcher();

    void setPathMatcher(String pathMatcher);

    /**
     * @return Path representing the directory where files are moved for awaiting processing.
     */
    Path getInProcessDirectory();

    void setProcessingDirectory(Path path);

    /**
     * @return Path representing the directory where files are moved once they've been processed successfully.
     */
    Path getSuccessDirectory();

    void setSuccessDirectory(Path path);

    /**
     * @return Path representing the directory where files are moved in case they could not be processed successfully.
     */
    Path getFailureDirectory();

    void setFailureDirectory(Path path);

    /**
     * @return a CronExpression that indicates the times at which the import directory should be scanned.
     */
    ScheduleExpression getScheduleExpression();

    void setScheduleExpression(ScheduleExpression scheduleExpression);

    /**
     * @return returns the type of the importer (name of the FileImporterFactory
     */
    String getImporterName();

    void setImporterName(String name);

    /**
     * @return returns the list of importer property specs
     */
    List<PropertySpec> getPropertySpecs();

    /**
     * @return returns the property spec of specified property name
     */
    PropertySpec getPropertySpec(String name);

    /**
     * @return returns the display name of property specified by name
     */

    String getPropertyDisplayName(String name);

    /**
     * @return returns the list of importer properties
     */
    List<FileImporterProperty> getImporterProperties();

    Map<String,Object> getProperties();

    void setProperty(String name, Object value);

    String getApplicationName();

    boolean isImporterAvailable();

    Finder<FileImportOccurrence> getFileImportOccurrences();

    Optional<FileImportOccurrence> getFileImportOccurrence(long occurrenceId);

    /**
     * Updates this instance.
     */
    void update();

    void delete();

    boolean isDeleted();

    Instant getObsoleteTime();

    default boolean isObsolete() {
        return getObsoleteTime() != null;
    }

}
