/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportDestination;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.FtpDestination;

import org.glassfish.hk2.api.ServiceLocator;

public class FtpDestinationInfoFactory extends AbstractFtpDestinationInfoFactory {

    @Override
    DestinationType getDestinationType() {
        return DestinationType.FTP;
    }

    @Override
    boolean isCorrectDestination(DataExportDestination destination) {
        return destination instanceof FtpDestination;
    }

    @Override
    public void create(ServiceLocator serviceLocator, ExportTask task, DestinationInfo info) {
        task.addFtpDestination(info.server, info.port, info.user, info.password, info.fileLocation, info.fileName, info.fileExtension);
    }

    @Override
    public Class<? extends DataExportDestination> getDestinationClass(ServiceLocator serviceLocator) {
        return FtpDestination.class;
    }
}


