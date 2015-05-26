package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataProcessorFactory;
import com.elster.jupiter.export.DataSelectorFactory;
import com.elster.jupiter.export.ReadingTypeDataExportTask;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.time.RelativePeriod;

import java.util.List;
import java.util.Optional;

interface IDataExportService extends DataExportService {

    DestinationSpec getDestination();

    Optional<DataProcessorFactory> getDataProcessorFactory(String name);

    IDataExportOccurrence createExportOccurrence(TaskOccurrence taskOccurrence);

    Optional<IDataExportOccurrence> findDataExportOccurrence(TaskOccurrence occurrence);

    Thesaurus getThesaurus();

    List<ReadingTypeDataExportTask> findExportTaskUsing(RelativePeriod relativePeriod);

    Optional<DataSelectorFactory> getDataSelectorFactory(String dataSelector);
}
