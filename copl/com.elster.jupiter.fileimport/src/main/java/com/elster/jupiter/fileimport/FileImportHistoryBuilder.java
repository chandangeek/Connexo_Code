/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport;

import java.time.Instant;

/**
 * Interface for builders for an FileImportHistory
 */
public interface FileImportHistoryBuilder {

    /**
     * Builds the ImportSchedule
     * @return
     */
    FileImportHistory create();
    /**
     *
     * @param importSchedule import service which performed file import via user interface
     * @return this
     */
    FileImportHistoryBuilder setImportSchedule(ImportSchedule importSchedule);

    /**
     *
     * @param userName name of the user who launched import
     * @return this
     */
    FileImportHistoryBuilder setUserName(String userName);

    /**
     *
     * @param fileName name of the imported file
     * @return this
     */
    FileImportHistoryBuilder setFileName(String fileName);

    /**
     *
     * @param uploadTime file upload time
     * @return this
     */
    FileImportHistoryBuilder setUploadTime(Instant uploadTime);
}
