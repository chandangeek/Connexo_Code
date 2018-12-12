/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportDestination;
import com.elster.jupiter.export.FtpDataExportDestination;

public abstract class AbstractFtpDestinationInfoFactory implements DestinationInfoFactory {

    abstract DestinationType getDestinationType();

    abstract boolean isCorrectDestination(DataExportDestination destination);

    @Override
    public DestinationInfo toInfo(DataExportDestination destination) {
        if (!isCorrectDestination(destination)) {
            throw new IllegalArgumentException();
        }
        FtpDataExportDestination ftpDestination = (FtpDataExportDestination) destination;
        DestinationInfo destinationInfo = new DestinationInfo();
        destinationInfo.type = getDestinationType();
        destinationInfo.id = destination.getId();
        destinationInfo.server = ftpDestination.getServer();
        destinationInfo.user = ftpDestination.getUser();
        destinationInfo.password = ftpDestination.getPassword();
        destinationInfo.fileLocation = ftpDestination.getFileLocation();
        destinationInfo.fileName = ftpDestination.getFileName();
        destinationInfo.fileExtension = ftpDestination.getFileExtension();
        destinationInfo.port = ftpDestination.getPort();
        return destinationInfo;
    }

    @Override
    public void update(DataExportDestination destination, DestinationInfo info) {
        if (!isCorrectDestination(destination)) {
            throw new IllegalArgumentException();
        }
        FtpDataExportDestination ftpDestination = (FtpDataExportDestination) destination;
        ftpDestination.setServer(info.server);
        ftpDestination.setUser(info.user);
        ftpDestination.setPassword(info.password);
        ftpDestination.setFileLocation(info.fileLocation);
        ftpDestination.setFileName(info.fileName);
        ftpDestination.setFileExtension(info.fileExtension);
        ftpDestination.setPort(info.port);
        ftpDestination.save();
    }

}
