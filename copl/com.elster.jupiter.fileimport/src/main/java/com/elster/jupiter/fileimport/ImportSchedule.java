package com.elster.jupiter.fileimport;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.time.ScheduleExpression;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Entity that models when a certain directory should be scanned for files to import and where to move them during different stages of the import cycle.
 */
@ProviderType
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
    Path getImportDirectory();

    /**
     * @return String representing the pattern to scan for new files to import
     */
    String getPathMatcher();

    /**
     * @return File representing the directory where files are moved for awaiting processing.
     */
    Path getInProcessDirectory();

    /**
     * @return File representing the directory where files are moved once they've been processed successfully.
     */
    Path getSuccessDirectory();

    /**
     * @return File representing the directory where files are moved in case they could not be processed successfully.
     */
    Path getFailureDirectory();

    /**
     * @return a CronExpression that indicates the times at which the import directory should be scanned.
     */
    ScheduleExpression getScheduleExpression();
    /**
     * Updates this instance.
     */
    void update();

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

    void setImportDirectory(Path path);

    void setFailureDirectory(Path path);

    void setSuccessDirectory(Path path);

    void setProcessingDirectory(Path path);

    void setImporterName(String name);

    void setPathMatcher(String pathMatcher);

    void setDestination(String destinationName);

    String getApplicationName();

    Instant getObsoleteTime();

    default boolean isObsolete() {
        return getObsoleteTime() != null;
    }

    void setActive(Boolean active);

    boolean isDeleted();

    boolean isImporterAvailable();

    Finder<FileImportOccurrence> getFileImportOccurrences();

    Optional<FileImportOccurrence> getFileImportOccurrence(long occurrenceId);

    long getVersion();
}
