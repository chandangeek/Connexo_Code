package com.elster.jupiter.export;

import java.util.List;

public interface FileDestination extends DataExportDestination {

    String TYPE_IDENTIFIER = "FILE";

    String getFileName();

    String getFileExtension();

    String getFileLocation();

    void send(List<FormattedExportData> data);

    void setFileLocation(String fileLocation);

    void setFileName(String fileName);

    void setFileExtension(String fileExtension);
}
