/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport;

import aQute.bnd.annotation.ProviderType;

/**
 * Interface to be implemented by classes that process file contents.
 */
@ProviderType
public interface FileImporter {

    /**
     * This will pass a FileImport to the implementing class that is in need of processing.
     * The FileImport will be in PROCESSING state, and the method should call markSuccess() or markFailure() on the fileImport.
     * Any exception thrown from this method will cause the fileImport to fail.
     * Care should be taken not to throw any exceptions out of this method after a call to markSuccess() or markFailure()
     * as this will rollback the state change, but not the location of the file.
     *
     * If the importer is doing it's own transaction management (see {@link FileImporterFactory#requiresTransaction}), any interaction
     * with the fileImportOccurrence, including logging, needs to happen <b>outside</b> of a transaction
     *
     * @param fileImportOccurrence the file import occurrence to handle
     */
    void process(FileImportOccurrence fileImportOccurrence);

}
