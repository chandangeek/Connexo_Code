package com.elster.jupiter.fileimport;

import java.io.InputStream;

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
public interface FileImport {

    /**
     * @return the importSchedule that caused this FileImport
     */
    ImportSchedule getImportSchedule();

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
    State getState();

    /**
     * Marks the file as successfully imported
     * @throws IllegalStateException if the current state is not PROCESSING
     */
    void markSuccess() throws IllegalStateException;

    /**
     * Marks the file as not successfully imported
     * @throws IllegalStateException if the current state is not PROCESSING
     */
    void markFailure();

    /**
     * @return the id
     */
    long getId();

    /**
     * Marks the file as being processed.
     * @throws IllegalStateException if the current state is not NEW
     */
    void prepareProcessing();
}
