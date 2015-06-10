package com.elster.jupiter.fileimport;

import com.elster.jupiter.domain.util.Finder;

import java.io.InputStream;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * A FileImport is an occurrence of one file being imported.
 * It can be in one of 4 states :
 * <ul>
 *     <li>NEW : when the file is first detected to be imported.</li>
 *     <li>PROCESSING : when the file is awaiting processing, or being processed</li>
 *     <li>SUCCESS : when the file was processed successfully</li>
 *     <li>FAILURE : when the file was not processed successfully.</li>
 * </ul>
 * FileImport shields the actual file from the code that does the actual processing of its contents,
 * allowing the underlying file system to vary as needed.
 */
public interface FileImportOccurrence {

    /**
     * Opens a new inputStream of the contents of the file.
     * @return
     */
    InputStream getContents();

    /**
     * @return the name of the file
     */
    String getFileName();

    /**
     * @return the current State
     */
    Status getStatus();

    /**
     * Marks the file as successfully imported
     * @throws IllegalStateException if the current state is not PROCESSING
     */
    void markSuccess(String message) throws IllegalStateException;


    /**
     * Marks the file as successfully imported but the import process encounter failures
     * @throws IllegalStateException if the current state is not PROCESSING
     */
    void markSuccessWithFailures(String message) throws IllegalStateException;

    /**
     * Marks the file as not successfully imported
     * @throws IllegalStateException if the current state is not PROCESSING
     */
    void markFailure(String message);

    /**
     * @return the id
     */
    long getId();

    /**
     * Marks the file as being processed.
     * @throws IllegalStateException if the current state is not NEW
     */
    void prepareProcessing();


    ImportSchedule getImportSchedule();

    Optional<Instant> getStartDate();

    Optional<Instant> getEndDate();

    Instant getTriggerDate();

    void setStartDate(Instant instant);
    void setEndDate(Instant instant);

    List<ImportLogEntry> getLogs();

    FileImportLogHandler createFileImportLogHandler();

    Logger getLogger();

    Finder<ImportLogEntry> getLogsFinder();

    String getMessage();

}
