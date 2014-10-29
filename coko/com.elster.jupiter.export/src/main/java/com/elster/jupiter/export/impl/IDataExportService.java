package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataProcessorFactory;
import com.elster.jupiter.messaging.DestinationSpec;

import java.util.List;
import java.util.Optional;

interface IDataExportService extends DataExportService {

    DestinationSpec getDestination();

    Optional<DataProcessorFactory> getDataProcessorFactory(String name);

}
