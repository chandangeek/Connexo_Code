package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportStrategy;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.export.ValidatedDataOption;
import com.elster.jupiter.time.RelativePeriod;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Optional;

import static com.elster.jupiter.util.Ranges.copy;

class DataExportStrategyImpl implements DataExportStrategy {

    private final boolean exportUpdate;
    private final PeriodBehaviour periodBehaviour;
    private final ValidatedDataOption validatedDataOption;
    private final RelativePeriod updatePeriod;
    private final RelativePeriod updateWindow;

    DataExportStrategyImpl(boolean exportUpdate, boolean exportContinuousData, ValidatedDataOption validatedDataOption, RelativePeriod updatePeriod, RelativePeriod updateWindow) {
        this.exportUpdate = exportUpdate;
        this.updatePeriod = updatePeriod;
        this.updateWindow = updateWindow;
        this.periodBehaviour = exportContinuousData ? PeriodBehaviour.CONTINUOUS : PeriodBehaviour.REQUESTED;
        this.validatedDataOption = validatedDataOption;
    }

    @Override
    public boolean isExportUpdate() {
        return exportUpdate;
    }

    @Override
    public boolean isExportContinuousData() {
        return periodBehaviour.isExportContinuousData();
    }

    @Override
    public ValidatedDataOption getValidatedDataOption() {
        return validatedDataOption;
    }

    @Override
    public Range<Instant> adjustedExportPeriod(DataExportOccurrence occurrence, ReadingTypeDataExportItem item) {
        return periodBehaviour.adjustedExportPeriod(occurrence, item);
    }

    @Override
    public Optional<RelativePeriod> getUpdatePeriod() {
        return Optional.ofNullable(updatePeriod);
    }

    @Override
    public Optional<RelativePeriod> getUpdateWindow() {
        return Optional.ofNullable(updateWindow);
    }

    private enum PeriodBehaviour {
        CONTINUOUS {
            @Override
            Range<Instant> adjustedExportPeriod(DataExportOccurrence occurrence, ReadingTypeDataExportItem item) {
                return item.getLastRun()
                        .map(instant -> copy(occurrence.getExportedDataInterval()).withOpenLowerBound(instant))
                        .orElse(occurrence.getExportedDataInterval());
            }
        }, REQUESTED {
            @Override
            Range<Instant> adjustedExportPeriod(DataExportOccurrence occurrence, ReadingTypeDataExportItem item) {
                return occurrence.getExportedDataInterval();
            }
        };

        abstract Range<Instant> adjustedExportPeriod(DataExportOccurrence occurrence, ReadingTypeDataExportItem item);

        boolean isExportContinuousData() {
            return CONTINUOUS.equals(this);
        }
    }
}
