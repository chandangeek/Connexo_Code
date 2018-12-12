/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.export.FtpDestination;
import com.elster.jupiter.ftpclient.FtpClientService;
import com.elster.jupiter.ftpclient.FtpSessionFactory;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.transaction.TransactionService;

import javax.inject.Inject;
import java.nio.file.FileSystem;
import java.time.Clock;

class FtpDestinationImpl extends AbstractFtpDataExportDestination implements FtpDestination {

    @Inject
    FtpDestinationImpl(DataModel dataModel, Clock clock, Thesaurus thesaurus, IDataExportService dataExportService, FileSystem fileSystem, DataVaultService dataVaultService, FtpClientService ftpClientService, TransactionService transactionService) {
        super(dataModel, clock, thesaurus, dataExportService, fileSystem, dataVaultService, ftpClientService, transactionService);
    }

    FtpDestinationImpl initialize(IExportTask task, String server, int port, String user, String password, String fileLocation, String fileName, String fileExtension) {
        super.doInitialize(task, server, port, user, password, fileLocation, fileName, fileExtension);
        return this;
    }

    static FtpDestinationImpl from(IExportTask task, DataModel dataModel, String server, int port, String user, String password, String fileLocation, String fileName, String fileExtension) {
        return dataModel.getInstance(FtpDestinationImpl.class).initialize(task, server, port, user, password, fileLocation, fileName, fileExtension);
    }

    @Override
    FtpSessionFactory getFtpSessionFactory() {
        return getFtpClientService().getFtpFactory(getServer(), getPort(), getUser(), getPassword());
    }

    String getMethodName() {
        return "ftp";
    }
}
