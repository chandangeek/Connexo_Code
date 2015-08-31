package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportStrategy;
import com.elster.jupiter.export.DefaultSelectorOccurrence;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.export.ValidatedDataOption;
import com.google.common.collect.Range;

import java.time.Instant;

import static com.elster.jupiter.util.Ranges.copy;

/**
* Copyrights EnergyICT
* Date: 17/11/2014
* Time: 17:07
*/
class DataExportStrategyImpl implements DataExportStrategy {

    private final boolean exportUpdate;
    private final PeriodBehaviour periodBehaviour;
    private final ValidatedDataOption validatedDataOption;

    DataExportStrategyImpl(boolean exportUpdate, boolean exportContinuousData, ValidatedDataOption validatedDataOption) {
        this.exportUpdate = exportUpdate;
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

    private enum PeriodBehaviour {
        CONTINUOUS {
            @Override
            Range<Instant> adjustedExportPeriod(DataExportOccurrence occurrence, ReadingTypeDataExportItem item) {
                return item.getLastRun()
                        .map(instant -> copy((occurrence.getDefaultSelectorOccurrence()
                                .map(DefaultSelectorOccurrence::getExportedDataInterval)
                                .orElse(Range.all())))
                                .withOpenLowerBound(instant))
                        .orElse(Range.all());
            }
        }, REQUESTED {
            @Override
            Range<Instant> adjustedExportPeriod(DataExportOccurrence occurrence, ReadingTypeDataExportItem item) {
                return occurrence.getDefaultSelectorOccurrence()
                        .map(DefaultSelectorOccurrence::getExportedDataInterval)
                        .orElse(Range.all());
            }
        };

        abstract Range<Instant> adjustedExportPeriod(DataExportOccurrence occurrence, ReadingTypeDataExportItem item);

        boolean isExportContinuousData() {
            return CONTINUOUS.equals(this);
        }
    }
}
