package com.elster.jupiter.validation.kpi;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

@ProviderType
public interface DataValidationKpiScore extends Comparable<DataValidationKpiScore> {

    long getTotalSuspects();

    long getChannelSuspects();

    long getRegisterSuspects();

    long getAllDataValidated();

    Instant getTimestamp();

    long getThresholdValidator();

    long getMissingValuesValidator();

    long getReadingQualitiesValidator();

    long getRegisterIncreaseValidator();

}