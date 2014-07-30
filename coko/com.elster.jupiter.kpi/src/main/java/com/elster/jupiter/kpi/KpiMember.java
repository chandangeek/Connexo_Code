package com.elster.jupiter.kpi;

import com.elster.jupiter.util.HasName;

import java.math.BigDecimal;
import java.util.Date;

public interface KpiMember extends HasName {


    boolean hasDynamicTarget();

    BigDecimal getTarget(Date date);

    boolean targetIsMinimum();

    boolean targetIsMaximum();
}
