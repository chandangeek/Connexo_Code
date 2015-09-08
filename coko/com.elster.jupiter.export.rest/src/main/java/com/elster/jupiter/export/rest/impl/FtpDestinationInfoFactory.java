package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportDestination;
import com.elster.jupiter.export.EmailDestination;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.FtpDestination;

public class FtpDestinationInfoFactory implements DestinationInfoFactory {
    @Override
    public void create(ExportTask task, DestinationInfo info) {
        task.addFtpDestination(info.server, info.user, info.password, info.fileLocation, info.fileName, info.fileExtension);
    }

    @Override
    public DestinationInfo toInfo(DataExportDestination destination) {
        if (!(destination instanceof FtpDestination)) {
            throw new IllegalArgumentException();
        }
        FtpDestination ftpDestination = (FtpDestination) destination;
        DestinationInfo destinationInfo = new DestinationInfo();
        destinationInfo.type = DestinationType.FTP;
        destinationInfo.id = destination.getId();
        destinationInfo.server = ftpDestination.getServer();
        destinationInfo.user = ftpDestination.getUser();
        destinationInfo.password = ftpDestination.getPassword();
        destinationInfo.fileLocation = ftpDestination.getFileLocation();
        destinationInfo.fileName = ftpDestination.getFileName();
        destinationInfo.fileExtension = ftpDestination.getFileExtension();
        return destinationInfo;
    }

    @Override
    public Class<? extends DataExportDestination> getDestinationClass() {
        return FtpDestination.class;
    }

    @Override
    public void update(DataExportDestination destination, DestinationInfo info) {
        if (!(destination instanceof FtpDestination)) {
            throw new IllegalArgumentException();
        }
        FtpDestination ftpDestination = (FtpDestination) destination;
        ftpDestination.setServer(info.server);
        ftpDestination.setUser(info.user);
        ftpDestination.setPassword(info.password);
        ftpDestination.setFileLocation(info.fileLocation);
        ftpDestination.setFileName(info.fileName);
        ftpDestination.setFileExtension(info.fileExtension);
        ftpDestination.save();
    }
}


