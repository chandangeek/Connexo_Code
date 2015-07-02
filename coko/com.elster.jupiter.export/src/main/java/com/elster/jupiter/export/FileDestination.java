package com.elster.jupiter.export;

public interface FileDestination extends DataExportDestination {

    String TYPE_IDENTIFIER = "LFILE";

    String getFileName();

    String getFileExtension();

    String getFileLocation();

    void setFileLocation(String fileLocation);

    void setFileName(String fileName);

    void setFileExtension(String fileExtension);
}
