package com.elster.jupiter.fileimport;

/**
 * Interface to be implemented by classes that process file contents.
 */
public interface FileImporter {

    /**
     * This will pass a FileImport to the implementing class that is in need of processing.
     * The FileImport will be in PROCESSING state, and the method should call markSuccess() or markFailure() on the fileImport.
     * Any exception thrown from this method will cause the dequeue of the message to be rolled back.
     * Care should be taken not to throw any exceptions out of this method after a call to markSuccess() or markFailure() as this will rollback the state change, but not the location of the file.
     *
     * @param fileImportOccurrence
     */
    void process(FileImportOccurrence fileImportOccurrence);

}
