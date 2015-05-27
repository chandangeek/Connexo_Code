package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportDestination;
import com.elster.jupiter.export.FileDestination;
import com.elster.jupiter.orm.DataModel;

/**
 * Created by igh on 22/05/2015.
 */
public class FileDestinationImpl extends AbstractDataExportDestination implements FileDestination {

    private String fileName;
    private String fileExtension;
    private String fileLocation;

    FileDestinationImpl(DataModel dataModel) {
        super(dataModel);
    }

    FileDestinationImpl init(String fileName, String fileExtension, String fileLocation) {
        this.fileName = fileName;
        this.fileExtension = fileExtension;
        this.fileLocation = fileLocation;
        return this;
    }
}
