/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImportHistory;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;

import javax.inject.Inject;
import java.time.Instant;

public class FileImportHistoryImpl implements FileImportHistory {

    private final DataModel dataModel;

    @SuppressWarnings("unused") // Managed by ORM
    private long id;

    private Reference<ImportSchedule> importSchedule = Reference.empty();
    private String userName;
    private Instant uploadTime;

    @Inject
    FileImportHistoryImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    FileImportHistoryImpl init(ImportSchedule importSchedule, String userName, Instant uploadTime) {
        this.importSchedule.set(importSchedule);
        this.userName = userName;
        this.uploadTime = uploadTime;
        return this;
    }

    static FileImportHistoryImpl from(DataModel dataModel, ImportSchedule importSchedule, String userName, Instant uploadTime) {
        return dataModel.getInstance(FileImportHistoryImpl.class).init(importSchedule, userName, uploadTime);
    }

    @Override
    public ImportSchedule getImportSchedule() {
        return importSchedule.get();
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public Instant getUploadTime() {
        return uploadTime;
    }

    @Override
    public void setImportSchedule(ImportSchedule importSchedule) {
        this.importSchedule.set(importSchedule);
    }

    @Override
    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public void setUploadTime(Instant uploadTime) {
        this.uploadTime = uploadTime;
    }
}
