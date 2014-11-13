package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportStatus;

/**
 * Copyrights EnergyICT
 * Date: 5/11/2014
 * Time: 10:22
 */
public interface IDataExportOccurrence extends DataExportOccurrence {

    void persist();

    void update();

    IReadingTypeDataExportTask getTask();

    void start();

    void end(DataExportStatus status);

    void end(DataExportStatus status, String message);
}
