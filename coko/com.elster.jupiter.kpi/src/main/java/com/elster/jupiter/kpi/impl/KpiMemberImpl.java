/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kpi.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.TimeSeriesDataStorer;
import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.kpi.Kpi;
import com.elster.jupiter.kpi.KpiEntry;
import com.elster.jupiter.kpi.TargetStorer;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.RangeBuilder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.elster.jupiter.util.streams.Predicates.not;

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
    private final EventService eventService;
    private final DataModel dataModel;

    // Audit fields
    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;

    @Inject
    KpiMemberImpl(IdsService idsService, EventService eventService, DataModel dataModel) {
        this.idsService = idsService;
        this.eventService = eventService;
        this.dataModel = dataModel;
    }

    KpiMemberImpl(IdsService idsService, EventService eventService, DataModel dataModel, KpiImpl kpi, String name) {
        this.idsService = idsService;
        this.eventService = eventService;
        this.dataModel = dataModel;
        this.kpi.set(kpi);
        this.name = name;
    }

    @Override
    public Kpi getKpi() {
        return kpi.get();
    }

    @Override
    public boolean hasDynamicTarget() {
        return dynamic;
    }

    @Override
    public BigDecimal getTarget(Instant date) {
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
        return target;
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
    public void score(Instant date, BigDecimal bigDecimal) {
        TimeSeriesDataStorer storer = idsService.createOverrulingStorer();
        storer.add(getTimeSeries(), date, bigDecimal, getTarget(date));
        storer.execute();
        KpiEntry kpiEntry = getScore(date).get();
        if (!kpiEntry.meetsTarget()) {
            eventService.postEvent(EventType.KPI_TARGET_MISSED.topic(), new KpiMissEventImpl(this, kpiEntry));
        }
    }

    @Override
    public void score(Map<Instant, BigDecimal> scores) {
        TimeSeriesDataStorer storer = idsService.createOverrulingStorer();
        Range<Instant> range = addScores(storer, scores);
        storer.execute();
        checkKpiScores(range);
    }

    @Override
    public Range<Instant> addScores(TimeSeriesDataStorer storer, Map<Instant, BigDecimal> scores) {
        TimeSeries timeSeries = getTimeSeries();
        RangeBuilder rangeBuilder = new RangeBuilder();
        scores.entrySet().forEach(entry -> {
            storer.add(timeSeries, entry.getKey(), entry.getValue(), getTarget(entry.getKey()));
            rangeBuilder.add(entry.getKey());
        });
        return rangeBuilder.getRange();
    }

    @Override
    public void checkKpiScores(Range<Instant> range) {
        getScores(range).stream().filter(not(KpiEntry::meetsTarget)).forEach(entry -> eventService.postEvent(EventType.KPI_TARGET_MISSED.topic(), new KpiMissEventImpl(this, entry)));
    }

    @Override
    public Optional<KpiEntry> getScore(Instant date) {
        java.util.Optional<TimeSeriesEntry> entry = getTimeSeries().getEntry(date);
        if (entry.isPresent()) {
            return Optional.of(new KpiEntryImpl(this, entry.get()));
        }
        return Optional.empty();
    }

    @Override
    public List<? extends KpiEntry> getScores(Range<Instant> range) {
        ImmutableList.Builder<KpiEntry> result = ImmutableList.builder();
        for (TimeSeriesEntry entry : getTimeSeries().getEntries(range)) {
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
        if (!dynamic) {
            throw new IllegalStateException("KpiMember : '" + getName() + "' : Cannot store targets for a KpiMember with static target.");
        }
        return new TargetStorer() {

            private final TimeSeriesDataStorer storer = idsService.createOverrulingStorer();

            @Override
            public TargetStorer add(Instant timestamp, BigDecimal target) {
                storer.add(getTimeSeries(), timestamp, null, target);
                return this;
            }

            @Override
            public void execute() {
                storer.execute();
            }
        };
    }

    @Override
    public void updateTarget(BigDecimal target) {
        if (dynamic) {
            throw new IllegalStateException("KpiMember : '" + getName() + "' : Cannot set static target for a KpiMember with dynamic target.");
        }
        this.targetValue = target;
        dataModel.mapper(IKpiMember.class).update(this);
    }

    int getPosition() {
        return position;
    }

    private static final class DynamicKpiTarget implements KpiTarget {

        private final TimeSeries timeSeries;

        private DynamicKpiTarget(TimeSeries timeSeries) {
            this.timeSeries = timeSeries;
        }

        @Override
        public BigDecimal get(Instant date) {
            java.util.Optional<TimeSeriesEntry> entry = timeSeries.getEntry(date);
            if (entry.isPresent()) {
                return entry.get().getBigDecimal(1);
            }
            return null;
        }
    }

    private static final class StaticKpiTarget implements KpiTarget {

        private final BigDecimal targetValue;

        private StaticKpiTarget(BigDecimal targetValue) {
            this.targetValue = targetValue;
        }

        @Override
        public BigDecimal get(Instant date) {
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
