package com.elster.jupiter.export;

import java.util.List;

/**
 * Created by igh on 22/05/2015.
 */
public interface DataExportDestination {

    ExportTask getTask();

    long getId();

    void save();

}
