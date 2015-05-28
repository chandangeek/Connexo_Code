package com.elster.jupiter.export;

import java.util.List;

/**
 * Created by igh on 22/05/2015.
 */
public interface DataExportDestination {

    ReadingTypeDataExportTask getTask();

    long getId();

    void save();

    void send(List<FormattedExportData> data);
}
