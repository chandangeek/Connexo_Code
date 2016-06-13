package com.elster.jupiter.validation.kpi;

import java.math.BigDecimal;
import java.time.Instant;


public interface DataValidationKpiScore extends Comparable<DataValidationKpiScore> {

    Instant getTimeStamp();

    BigDecimal getSuccess();

    BigDecimal getOngoing();

    BigDecimal getFailed();

}