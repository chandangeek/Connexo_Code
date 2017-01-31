/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export;

/**
 * Wraps an underlying exception, indicating failure of the data export. Data export will stop processing items.
 */
public class FatalDataExportException extends RuntimeException {

    public FatalDataExportException(RuntimeException cause) {
        super(cause.getMessage(), cause);
    }

    public String getLocalizedMessage() {
        return getCause().getLocalizedMessage();
    }
}
