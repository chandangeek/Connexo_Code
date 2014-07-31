package com.elster.jupiter.kpi;

import java.math.BigDecimal;
import java.util.Date;

public interface KpiEntry {

    Date getTimestamp();

    BigDecimal getScore();

    BigDecimal getTarget();

    boolean meetsTarget();
}
