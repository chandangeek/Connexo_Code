package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.time.RelativePeriod;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

interface IDataExportService extends DataExportService {

    DestinationSpec getDestination();

    IDataExportOccurrence createExportOccurrence(TaskOccurrence taskOccurrence);

    Optional<IDataExportOccurrence> findDataExportOccurrence(TaskOccurrence occurrence);

    Thesaurus getThesaurus();

    List<ExportTask> findExportTaskUsing(RelativePeriod relativePeriod);

    Path getTempDirectory();

    LocalFileWriter getLocalFileWriter();
}
