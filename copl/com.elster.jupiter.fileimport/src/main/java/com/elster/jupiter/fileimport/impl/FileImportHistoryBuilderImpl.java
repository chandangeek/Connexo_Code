/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImportHistory;
import com.elster.jupiter.fileimport.FileImportHistoryBuilder;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.orm.DataModel;

import java.time.Instant;

public class FileImportHistoryBuilderImpl implements FileImportHistoryBuilder {

    private ImportSchedule importSchedule;
    private String userName;
    private String fileName;
    private Instant uploadTime;
    private DataModel dataModel;

    FileImportHistoryBuilderImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public FileImportHistory create() {
        FileImportHistoryImpl fileImportHistory = FileImportHistoryImpl.from(dataModel, importSchedule, userName, fileName, uploadTime);
        fileImportHistory.save();
        return fileImportHistory;
    }

    @Override
    public FileImportHistoryBuilder setImportSchedule(ImportSchedule importSchedule) {
        this.importSchedule = importSchedule;
        return this;
    }

    @Override
    public FileImportHistoryBuilder setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    @Override
    public FileImportHistoryBuilder setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    @Override
    public FileImportHistoryBuilder setUploadTime(Instant uploadTime) {
        this.uploadTime = uploadTime;
        return this;
    }
}
