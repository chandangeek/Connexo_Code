package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataProcessorFactory;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.tasks.TaskOccurrence;

import java.util.Optional;

interface IDataExportService extends DataExportService {

    DestinationSpec getDestination();

    Optional<DataProcessorFactory> getDataProcessorFactory(String name);

    DataExportOccurrence createExportOccurrence(TaskOccurrence taskOccurrence);

}
