package com.elster.jupiter.validation.impl.kpi;


import com.elster.jupiter.kpi.KpiEntry;
import com.elster.jupiter.kpi.KpiMember;
import com.elster.jupiter.validation.kpi.DataValidationKpiScore;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;


public final class DataValidationKpiScoreImpl implements DataValidationKpiScore {

    private final Instant timestamp;
    private final  Map<MonitoredDataValidationKpiMemberTypes, BigDecimal> entries;

    public DataValidationKpiScoreImpl(Instant timestamp,  Map<MonitoredDataValidationKpiMemberTypes, BigDecimal> entries) {
        this.timestamp = timestamp;
        this.entries = entries;
    }

    @Override
    public Instant getTimestamp() {
        return this.timestamp;
    }


    private BigDecimal getValue(MonitoredDataValidationKpiMemberTypes member) {
        return entries.get(member);
    }

    @Override
    public BigDecimal getTotalSuspects() {
        return this.getValue(MonitoredDataValidationKpiMemberTypes.SUSPECT);
    }

    @Override
    public BigDecimal getChannelSuspects() {
        return this.getValue(MonitoredDataValidationKpiMemberTypes.CHANNEL);
    }

    @Override
    public BigDecimal getThresholdValidator() {
        return this.getValue(MonitoredDataValidationKpiMemberTypes.THRESHOLDVALIDATOR);
    }

    @Override
    public BigDecimal getMissingValuesValidator() {
        return this.getValue(MonitoredDataValidationKpiMemberTypes.MISSINGVALUESVALIDATOR);
    }

    @Override
    public BigDecimal getReadingQualitiesValidator() {
        return this.getValue(MonitoredDataValidationKpiMemberTypes.READINGQUALITIESVALIDATOR);
    }

    @Override
    public BigDecimal getRegisterIncreaseValidator() {
        return this.getValue(MonitoredDataValidationKpiMemberTypes.REGISTERINCREASEVALIDATOR);
    }

    @Override
    public BigDecimal getRegisterSuspects() {
        return this.getValue(MonitoredDataValidationKpiMemberTypes.REGISTER);
    }

    @Override
    public BigDecimal getAllDataValidated() {
        return this.getValue(MonitoredDataValidationKpiMemberTypes.ALLDATAVALIDATED);
    }

    @Override
    public int compareTo(DataValidationKpiScore other) {
        return this.getTimestamp().compareTo(other.getTimestamp());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        DataValidationKpiScoreImpl that = (DataValidationKpiScoreImpl) other;
        return this.timestamp.equals(that.timestamp);
    }

    @Override
    public int hashCode() {
        return this.timestamp.hashCode();
    }

}
