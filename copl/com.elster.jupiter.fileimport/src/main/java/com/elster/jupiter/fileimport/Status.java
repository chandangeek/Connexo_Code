package com.elster.jupiter.fileimport;

/**
 * Enumeration of the states of a FileImportOccurrence.
 * A FileImportOccurrence starts out as new. When moved to processing becomes PROCESSING and upon completion becomes either SUCCESS or FAILURE, depending on the success of processing.
 */
public enum Status {
    NEW("New"),
    PROCESSING("Busy"),
    SUCCESS("Success"),
    SUCCESS_WITH_FAILURES("Success with failures"),
    FAILURE("Failure");

    private String name;

    Status(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
