package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportStatus;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.util.time.Interval;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Optional;

class DataExportOccurrenceImpl implements DataExportOccurrence {

    private Reference<TaskOccurrence> taskOccurrence = ValueReference.absent();
    private Instant startDate;
    private Instant endDate;
    private Interval exportedDataInterval;
    private Interval.EndpointBehavior exportedDataBoundaryType;
    private DataExportStatus status = DataExportStatus.BUSY;

    private transient Range<Instant> exportedDataRange;

    @Override
    public Instant getStartDate() {
        return startDate;
    }

    @Override
    public Optional<Instant> getEndDate() {
        return Optional.ofNullable(endDate);
    }

    @Override
    public DataExportStatus getStatus() {
        return status;
    }

    @Override
    public Range<Instant> getExportedDataInterval() {
        if (exportedDataRange == null) {
            exportedDataRange = exportedDataBoundaryType.toRange(exportedDataInterval);
        }
        return exportedDataRange;
    }
}
