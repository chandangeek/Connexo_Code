package com.energyict.mdc.device.data.kpi;


import java.math.BigDecimal;
import java.time.Instant;

public interface DataValidationKpiScore {

    Instant getTimeStamp();

    BigDecimal getSuccess();

    BigDecimal getOngoing();

    BigDecimal getFailed();

}
