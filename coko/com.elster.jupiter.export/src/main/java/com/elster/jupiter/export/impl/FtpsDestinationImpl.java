package com.elster.jupiter.export.impl;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.FtpsDestination;
import com.elster.jupiter.ftpclient.FtpClientService;
import com.elster.jupiter.ftpclient.FtpSessionFactory;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.transaction.TransactionService;

import javax.inject.Inject;
import java.nio.file.FileSystem;
import java.time.Clock;

public class FtpsDestinationImpl extends AbstractFtpDataExportDestination implements FtpsDestination {

    @Inject
    FtpsDestinationImpl(DataModel dataModel, Clock clock, Thesaurus thesaurus, DataExportService dataExportService, FileSystem fileSystem, DataVaultService dataVaultService, FtpClientService ftpClientService, TransactionService transactionService) {
        super(dataModel, clock, thesaurus, dataExportService, fileSystem, dataVaultService, ftpClientService, transactionService);
    }

    static FtpsDestinationImpl from(IExportTask task, DataModel dataModel, String server, int port, String user, String password, String fileLocation, String fileName, String fileExtension) {
        return (FtpsDestinationImpl) dataModel.getInstance(FtpsDestinationImpl.class).init(task, server, port, user, password, fileLocation, fileName, fileExtension);
    }

    @Override
    FtpSessionFactory getFtpSessionFactory() {
        return getFtpClientService().getFtpsFactory(getServer(), getPort(), getUser(), getPassword());
    }
}
