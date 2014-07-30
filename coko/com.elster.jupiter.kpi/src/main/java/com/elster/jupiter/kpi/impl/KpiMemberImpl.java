package com.elster.jupiter.kpi.impl;

import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.kpi.Kpi;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.google.common.base.Optional;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Date;

class KpiMemberImpl implements IKpiMember {

    private Reference<Kpi> kpi = ValueReference.absent();
    private int position;
    private String name;
    private boolean dynamic;
    private BigDecimal targetValue;
    private boolean targetIsMinimum;
    private Reference<TimeSeries> timeSeries = ValueReference.absent();

    private transient KpiTarget target;

    @Inject
    KpiMemberImpl() {
    }

    KpiMemberImpl(KpiImpl kpi, String name) {
        this.kpi.set(kpi);
        this.name = name;
    }

    @Override
    public boolean hasDynamicTarget() {
        return dynamic;
    }

    @Override
    public BigDecimal getTarget(Date date) {
        return getTarget().get(date);
    }

    private KpiTarget getTarget() {
        if (target == null) {
            target = initTarget();
        }
        return target;
    }

    private KpiTarget initTarget() {
        if (target == null) {
            target = dynamic ? new DynamicKpiTarget(getTimeSeries()) : new StaticKpiTarget(targetValue);
        }
        return  target;
    }

    @Override
    public boolean targetIsMinimum() {
        return targetIsMinimum;
    }

    @Override
    public boolean targetIsMaximum() {
        return !targetIsMinimum;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setTimeSeries(TimeSeries timeSeries) {
        this.timeSeries.set(timeSeries);
    }

    @Override
    public TimeSeries getTimeSeries() {
        return timeSeries.get();
    }

    private static class DynamicKpiTarget implements KpiTarget {

        private final TimeSeries timeSeries;

        private DynamicKpiTarget(TimeSeries timeSeries) {
            this.timeSeries = timeSeries;
        }

        @Override
        public BigDecimal get(Date date) {
            Optional<TimeSeriesEntry> entry = timeSeries.getEntry(date);
            if (entry.isPresent()) {
                entry.get().getBigDecimal(1);
            }
            return null;
        }
    }

    private static class StaticKpiTarget implements KpiTarget {

        private final BigDecimal targetValue;

        private StaticKpiTarget(BigDecimal targetValue) {
            this.targetValue = targetValue;
        }

        @Override
        public BigDecimal get(Date date) {
            return targetValue;
        }
    }

    KpiMemberImpl setDynamicTarget() {
        dynamic = true;
        targetValue = null;
        return this;
    }

    KpiMemberImpl setStatictarget(BigDecimal targetValue) {
        dynamic = false;
        this.targetValue = targetValue;
        return this;
    }

    KpiMemberImpl asMinimum() {
        targetIsMinimum = true;
        return this;
    }

    KpiMemberImpl asMaximum() {
        targetIsMinimum = false;
        return this;
    }
}
