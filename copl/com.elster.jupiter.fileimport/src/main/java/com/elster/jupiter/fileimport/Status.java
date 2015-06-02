package com.elster.jupiter.fileimport;

/**
 * Enumeration of the states of a FileImport.
 * A FileImport starts out as new. When moved to processing becomes PROCESSING and upon completion becomes either SUCCESS or FAILURE, depending on the success of processing.
 */
public enum Status {
    NEW, PROCESSING, SUCCESS, FAILURE
}
