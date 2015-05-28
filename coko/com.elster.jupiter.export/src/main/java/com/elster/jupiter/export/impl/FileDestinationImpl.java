package com.elster.jupiter.export.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.export.DataExportDestination;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.FatalDataExportException;
import com.elster.jupiter.export.FileDestination;
import com.elster.jupiter.export.FileUtils;
import com.elster.jupiter.export.FormattedExportData;
import com.elster.jupiter.fileimport.FileIOException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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

    FileDestinationImpl init(String fileName, String fileExtension, String fileLocation) {
        this.fileName = fileName;
        this.fileExtension = fileExtension;
        this.fileLocation = fileLocation;
        return this;
    }

    public void send(List<FormattedExportData> data) {
        FileUtils fileUtils = new FileUtils(this.getFileSystem(), this.getThesaurus(), this.getDataExportService(), this.getAppService());
        fileUtils.createFile(data, fileName, fileExtension, fileLocation);
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public String getFileLocation() {
        return fileLocation;
    }
}
