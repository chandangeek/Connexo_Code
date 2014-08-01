package com.elster.jupiter.kpi;

import com.elster.jupiter.util.HasName;
import com.elster.jupiter.util.time.Interval;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public interface KpiMember extends HasName {

    Kpi getKpi();

    boolean hasDynamicTarget();

    BigDecimal getTarget(Date date);

    boolean targetIsMinimum();

    boolean targetIsMaximum();

    void score(Date date, BigDecimal bigDecimal);

    List<? extends KpiEntry> getScores(Interval interval);

    TargetStorer getTargetStorer();
}
