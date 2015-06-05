package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportDestination;
import com.elster.jupiter.export.ExportTask;

interface DestinationInfoFactory {

    void create(ExportTask task, DestinationInfo info);

    DestinationInfo toInfo(DataExportDestination destination);

    Class<? extends DataExportDestination> getDestinationClass();

    void update(DataExportDestination destination, DestinationInfo info);
}
