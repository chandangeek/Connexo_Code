package com.elster.jupiter.export.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.FileDestination;
import com.elster.jupiter.export.FormattedExportData;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import java.nio.file.FileSystem;
import java.util.List;

/**
 * Created by igh on 22/05/2015.
 */
public class FileDestinationImpl extends AbstractDataExportDestination implements FileDestination {

    private String fileName;
    private String fileExtension;
    private String fileLocation;


    @Inject
    FileDestinationImpl(DataModel dataModel, Thesaurus thesaurus, DataExportService dataExportService, AppService appService, FileSystem fileSystem) {
        super(dataModel, thesaurus, dataExportService, appService, fileSystem);
    }

    FileDestinationImpl init(IExportTask task, String fileLocation, String fileName, String fileExtension) {
        initTask(task);
        this.fileName = fileName;
        this.fileExtension = fileExtension;
        this.fileLocation = fileLocation;
        return this;
    }

    static FileDestinationImpl from(IExportTask task, DataModel dataModel, String fileLocation, String fileName, String fileExtension) {
        return dataModel.getInstance(FileDestinationImpl.class).init(task, fileLocation, fileName, fileExtension);
    }

    @Override
    public void send(List<FormattedExportData> data) {
        FileUtils fileUtils = new FileUtils(this.getFileSystem(), this.getThesaurus(), this.getDataExportService(), this.getAppService());
        fileUtils.createFile(data, fileName, fileExtension, fileLocation);
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public String getFileExtension() {
        return fileExtension;
    }

    @Override
    public String getFileLocation() {
        return fileLocation;
    }
}
