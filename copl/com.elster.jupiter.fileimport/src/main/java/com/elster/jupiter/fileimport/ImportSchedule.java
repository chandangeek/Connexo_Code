package com.elster.jupiter.fileimport;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.time.ScheduleExpression;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Entity that models when a certain directory should be scanned for files to import and where to move them during different stages of the import cycle.
 */
public interface ImportSchedule {

    /**
     * @return this entity's id
     */
    long getId();


    /**
     * @return import schedule status
     */

    boolean isActive();

    /**
     * @return import schedule name
     */
    String getName();

    /**
     * @return the Destination on which to post a message when a file needs processing.
     */
    DestinationSpec getDestination();

    /**
     * @return File representing the directory to scan for new files to import
     */
    File getImportDirectory();

    /**
     * @return String representing the pattern to scan for new files to import
     */
    String getPathMatcher();

    /**
     * @return File representing the directory where files are moved for awaiting processing.
     */
    File getInProcessDirectory();

    /**
     * @return File representing the directory where files are moved once they've been processed successfully.
     */
    File getSuccessDirectory();

    /**
     * @return File representing the directory where files are moved in case they could not be processed successfully.
     */
    File getFailureDirectory();

    /**
     * @return a CronExpression that indicates the times at which the import directory should be scanned.
     */
    ScheduleExpression getScheduleExpression();
    /**
     * Persists or updates this instance.
     */
    void save();

    /**
     * @param file
     * @return creates a new FileImport instance for the given file.
     */
    FileImportOccurrence createFileImportOccurrence(File file);

    /**
     * @return returns the type of the importer
     */
    String getImporterName();

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

    void setProperty(String name, Object value);

    Map<String,Object> getProperties();

    void delete();

    void setName(String name);

    void setScheduleExpression(ScheduleExpression scheduleExpression);

    void setImportDirectory(File file);

    void setFailureDirectory(File file);

    void setSuccessDirectory(File file);

    void setProcessingDirectory(File file);

    void setImporterName(String name);

    void setPathMatcher(String pathMatcher);

    void setDestination(String destinationName);

    String getApplicationName();

    void setActive(Boolean active);

    Finder<FileImportOccurrence> getFileImportOccurrences();

    Optional<FileImportOccurrence> getFileImportOccurrence(long occurrenceId);
}
