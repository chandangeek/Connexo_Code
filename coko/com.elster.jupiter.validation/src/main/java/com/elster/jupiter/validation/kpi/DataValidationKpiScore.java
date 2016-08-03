package com.elster.jupiter.validation.kpi;

import java.math.BigDecimal;
import java.time.Instant;


public interface DataValidationKpiScore extends Comparable<DataValidationKpiScore> {

    BigDecimal getTotalSuspects();

    BigDecimal getChannelSuspects();

    BigDecimal getRegisterSuspects();

    BigDecimal getAllDataValidated();

    Instant getTimestamp();

    BigDecimal getThresholdValidator();

    BigDecimal getMissingValuesValidator();

    BigDecimal getReadingQualitiesValidator();

    BigDecimal getRegisterIncreaseValidator();

}