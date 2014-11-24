package com.elster.jupiter.export;

import com.elster.jupiter.nls.LocalizedException;

/**
 * Wraps an underlying exception, indicating failure of processing the current DataExportItem. Processing subsequent items may continue.
 */
public class DataExportException extends RuntimeException {

    public DataExportException(LocalizedException cause) {
        super(cause.getMessage(), cause);
    }
}
