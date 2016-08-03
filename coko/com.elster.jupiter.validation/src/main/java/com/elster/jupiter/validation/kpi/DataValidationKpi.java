package com.elster.jupiter.validation.kpi;

import com.elster.jupiter.kpi.Kpi;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.validation.impl.kpi.MonitoredDataValidationKpiMemberTypes;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ProviderType
public interface DataValidationKpi extends HasId {

    Optional<TemporalAmount> dataValidationKpiCalculationIntervalLength();

    void setFrequency(TemporalAmount intervalLength);

    TemporalAmount getFrequency();

    EndDeviceGroup getDeviceGroup();

    void delete();

    Optional<DataValidationKpiScore> getDataValidationKpiScores(long deviceId, Range<Instant> interval);

    Optional<Instant> getLatestCalculation();

    List<DataValidationKpiChild> getDataValidationKpiChildren();

    void dropDataValidationKpi();

    long getVersion();

}

