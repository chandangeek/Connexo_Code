/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport;

import com.elster.jupiter.domain.util.Finder;

import aQute.bnd.annotation.ProviderType;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * A FileImport is an occurrence of one file being imported.
 * It can be in one of 5 states :
 * <ul>
 *     <li>NEW : when the file is first detected to be imported.</li>
 *     <li>PROCESSING : when the file is awaiting processing, or being processed</li>
 *     <li>SUCCESS : when the file was processed successfully</li>
 *     <li>SUCCESS_WITH_FAILURES: when the file was processed successfull, but some errors where caught</li>
 *     <li>FAILURE : when the file was not processed successfully.</li>
 * </ul>
 * FileImport shields the actual file from the code that does the actual processing of its contents,
 * allowing the underlying file system to vary as needed.
 */
@ProviderType
public interface FileImportOccurrence {

    /**
     * Opens a new inputStream of the contents of the file.
     *
     * @return The InputStream
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
     * Returns the name of the status of this FileImportOccurrence
     * in the user's preferred language.
     *
     * @return The name of the status of this FileImportOccurrence
     */
    String getStatusName();

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

    ImportSchedule getImportSchedule();

    Optional<Instant> getStartDate();

    Optional<Instant> getEndDate();

    Instant getTriggerDate();

    List<ImportLogEntry> getLogs();

    Logger getLogger();

    Finder<ImportLogEntry> getLogsFinder();

    String getMessage();

}