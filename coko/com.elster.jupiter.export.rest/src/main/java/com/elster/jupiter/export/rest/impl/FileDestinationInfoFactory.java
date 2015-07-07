package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportDestination;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.FileDestination;

class FileDestinationInfoFactory implements DestinationInfoFactory {
    @Override
    public void create(ExportTask task, DestinationInfo info) {
        task.addFileDestination(info.fileLocation, info.fileName, info.fileExtension);
    }

    @Override
    public DestinationInfo toInfo(DataExportDestination destination) {
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
    public Class<? extends DataExportDestination> getDestinationClass() {
        return FileDestination.class;
    }

    @Override
    public void update(DataExportDestination destination, DestinationInfo info) {
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
