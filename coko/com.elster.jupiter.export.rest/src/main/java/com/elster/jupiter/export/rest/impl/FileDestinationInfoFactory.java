/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportDestination;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.FileDestination;

import org.glassfish.hk2.api.ServiceLocator;

class FileDestinationInfoFactory implements DestinationInfoFactory {
    @Override
    public void create(ServiceLocator serviceLocator, ExportTask task, DestinationInfo info) {
        task.addFileDestination(info.fileLocation, info.fileName, info.fileExtension);
    }

    @Override
    public DestinationInfo toInfo(ServiceLocator serviceLocator, DataExportDestination destination) {
        if (!(destination instanceof FileDestination)) {
            throw new IllegalArgumentException();
        }
        FileDestination fileDestination = (FileDestination) destination;
        DestinationInfo destinationInfo = new DestinationInfo();
        destinationInfo.type = DestinationType.FILE;
        destinationInfo.id = destination.getId();
        destinationInfo.fileLocation = fileDestination.getFileLocation();
        destinationInfo.fileName = fileDestination.getFileName();
        destinationInfo.fileExtension = fileDestination.getFileExtension();
        return destinationInfo;
    }

    @Override
    public Class<? extends DataExportDestination> getDestinationClass(ServiceLocator serviceLocator) {
        return FileDestination.class;
    }

    @Override
    public void update(ServiceLocator serviceLocator, DataExportDestination destination, DestinationInfo info) {
        if (!(destination instanceof FileDestination)) {
            throw new IllegalArgumentException();
        }
        FileDestination fileDestination = (FileDestination) destination;
        fileDestination.setFileLocation(info.fileLocation);
        fileDestination.setFileName(info.fileName);
        fileDestination.setFileExtension(info.fileExtension);
        fileDestination.save();
    }
}
