/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportDestination;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.FtpsDestination;

public class FtpsDestinationInfoFactory extends AbstractFtpDestinationInfoFactory {

    @Override
    DestinationType getDestinationType() {
        return DestinationType.FTPS;
    }

    @Override
    boolean isCorrectDestination(DataExportDestination destination) {
        return destination instanceof FtpsDestination;
    }

    @Override
    public void create(ExportTask task, DestinationInfo info) {
        task.addFtpsDestination(info.server, info.port, info.user, info.password, info.fileLocation, info.fileName, info.fileExtension);
    }

    @Override
    public Class<? extends DataExportDestination> getDestinationClass() {
        return FtpsDestination.class;
    }

}
