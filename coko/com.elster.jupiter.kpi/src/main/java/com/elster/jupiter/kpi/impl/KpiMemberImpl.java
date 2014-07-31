package com.elster.jupiter.kpi.impl;

import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.TimeSeriesDataStorer;
import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.kpi.Kpi;
import com.elster.jupiter.kpi.KpiEntry;
import com.elster.jupiter.kpi.TargetStorer;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Interval;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

class KpiMemberImpl implements IKpiMember {

    private Reference<Kpi> kpi = ValueReference.absent();
    private int position;
    private String name;
    private boolean dynamic;
    private BigDecimal targetValue;
    private boolean targetIsMinimum;
    private Reference<TimeSeries> timeSeries = ValueReference.absent();

    private transient KpiTarget target;

    private final IdsService idsService;

    @Inject
    KpiMemberImpl(IdsService idsService) {
        this.idsService = idsService;
    }

    KpiMemberImpl(IdsService idsService, KpiImpl kpi, String name) {
        this.idsService = idsService;
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
    public void score(Date date, BigDecimal bigDecimal) {
        TimeSeriesDataStorer storer = idsService.createStorer(true);
        storer.add(getTimeSeries(), date, bigDecimal, getTarget(date));
        storer.execute();
    }

    @Override
    public List<? extends KpiEntry> getScores(Interval interval) {
        ImmutableList.Builder<KpiEntry> result = ImmutableList.builder();
        for (TimeSeriesEntry entry : getTimeSeries().getEntries(interval)) {
            result.add(new KpiEntryImpl(this, entry));
        }
        return result.build();
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

    @Override
    public TargetStorer getTargetStorer() {
        return new TargetStorer() {

            private final TimeSeriesDataStorer storer = idsService.createStorer(true);

            @Override
            public TargetStorer add(Date timestamp, BigDecimal target) {
                storer.add(getTimeSeries(), timestamp, null, target);
                return this;
            }

            @Override
            public void execute() {
                storer.execute();
            }
        };
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
                return entry.get().getBigDecimal(1);
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
