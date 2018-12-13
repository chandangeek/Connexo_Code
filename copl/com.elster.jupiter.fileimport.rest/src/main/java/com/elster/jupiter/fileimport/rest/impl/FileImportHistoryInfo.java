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

    public static FileImportHistoryInfo from(FileImportHistory fileImportHistory) {
        FileImportHistoryInfo fileImportHistoryInfo = new FileImportHistoryInfo();
        fileImportHistoryInfo.importScheduleId = fileImportHistory.getImportSchedule().getId();
        fileImportHistoryInfo.userName = fileImportHistory.getUserName();
        fileImportHistoryInfo.uploadTime = fileImportHistory.getUploadTime();

        return fileImportHistoryInfo;
    }
}
