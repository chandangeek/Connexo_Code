/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.rest.impl;

import com.elster.jupiter.fileimport.FileImportHistory;

import java.time.Instant;

public class FileImportHistoryInfo {
    public Long id;

    public Long importScheduleId;
    public String userName;
    public Instant uploadTime;

    public FileImportHistoryInfo() {
    }

    public FileImportHistoryInfo from(FileImportHistory fileImportHistory) {
        this.importScheduleId = fileImportHistory.getImportSchedule().getId();
        this.userName = fileImportHistory.getUserName();
        this.uploadTime = fileImportHistory.getUploadTime();
        return this;
    }
}
