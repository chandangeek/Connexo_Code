/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.export.DataExportDestination;
import com.elster.jupiter.export.EmailDestination;
import com.elster.jupiter.export.FtpDestination;
import com.elster.jupiter.export.FtpsDestination;
import com.elster.jupiter.export.FileDestination;
import com.elster.jupiter.export.SftpDestination;
import com.elster.jupiter.export.WebServiceDestination;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.transaction.TransactionService;

import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;
import java.nio.file.FileSystem;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;


public abstract class AbstractDataExportDestination implements IDataExportDestination {

    static final Map<String, Class<? extends DataExportDestination>> IMPLEMENTERS = new ImmutableMap.Builder<String, Class<? extends DataExportDestination>>()
            .put(FileDestination.TYPE_IDENTIFIER, LocalFileDestinationImpl.class)
            .put(EmailDestination.TYPE_IDENTIFIER, EmailDestinationImpl.class)
            .put(FtpDestination.TYPE_IDENTIFIER, FtpDestinationImpl.class)
            .put(FtpsDestination.TYPE_IDENTIFIER, FtpsDestinationImpl.class)
            .put(SftpDestination.TYPE_IDENTIFIER, SftpDestinationImpl.class)
            .put(WebServiceDestination.TYPE_IDENTIFIER, WebServiceDestinationImpl.class)
            .build();

    private long id;
    private Reference<IExportTask> task = ValueReference.absent();
    private final DataModel dataModel;
    private final Thesaurus thesaurus;
    private final IDataExportService dataExportService;
    private final TransactionService transactionService;
    private final FileSystem fileSystem;
    private final Clock clock;
    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;

    @Inject
    AbstractDataExportDestination(DataModel dataModel, Clock clock, Thesaurus thesaurus, IDataExportService dataExportService,
                                  FileSystem fileSystem, TransactionService transactionService) {
        this.dataModel = dataModel;
        this.clock = clock;
        this.thesaurus = thesaurus;
        this.dataExportService = dataExportService;
        this.fileSystem = fileSystem;
        this.transactionService = transactionService;
    }

    public IExportTask getTask() {
        return this.task.get();
    }

    void initTask(IExportTask task) {
        this.task.set(task);
    }

    @Override
    public long getId() {
        return id;
    }

    public void save() {
        if (getId() == 0) {
            doPersist();
            return;
        }
        doUpdate();
    }

    private void doPersist() {
        Save.CREATE.save(dataModel, this);
    }

    private void doUpdate() {
        Save.UPDATE.save(dataModel, this);
    }

    public void delete() {
        doUpdate();
    }

    final Thesaurus getThesaurus() {
        return this.thesaurus;
    }

    final IDataExportService getDataExportService() {
        return dataExportService;
    }

    final FileSystem getFileSystem() {
        return fileSystem;
    }

    final Clock getClock() {
        return clock;
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public Instant getCreateTime() {
        return createTime;
    }

    @Override
    public Instant getModTime() {
        return modTime;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    protected TransactionService getTransactionService() {
        return this.transactionService;
    }

}
