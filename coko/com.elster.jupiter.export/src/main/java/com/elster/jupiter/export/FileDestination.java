package com.elster.jupiter.export;

import java.util.List;

/**
 * Created by igh on 26/05/2015.
 */
public interface FileDestination extends DataExportDestination {

    String TYPE_IDENTIFIER = "FILE";

    String getFileName();

    String getFileExtension();

    String getFileLocation();

    void send(List<FormattedExportData> data);
}
