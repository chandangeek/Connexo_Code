/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kpi.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.TimeSeriesDataStorer;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.kpi.Kpi;
import com.elster.jupiter.kpi.KpiMember;
import com.elster.jupiter.kpi.KpiUpdater;
import com.elster.jupiter.orm.DataModel;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;

import static com.elster.jupiter.util.streams.Predicates.not;

class KpiImpl implements Kpi {

    private long id;
    private String name;
    private List<IKpiMember> members = new ArrayList<>();

    private transient TimeZone timeZone;
    private transient TemporalAmount intervalLength;

    private final DataModel dataModel;
    private final IdsService idsService;
    private final IKpiService kpiService;
    private final EventService eventService;

    // Audit fields
    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;

    @Inject
    KpiImpl(DataModel dataModel, IdsService idsService, IKpiService iKpiService, EventService eventService) {
        this.dataModel = dataModel;
        this.idsService = idsService;
        this.kpiService = iKpiService;
        this.eventService = eventService;
    }

    KpiImpl init(String name, TimeZone timeZone, TemporalAmount intervalLength) {
        this.name = name;
        this.timeZone = timeZone;
        this.intervalLength = intervalLength;
        return this;
    }

    static KpiImpl from(DataModel dataModel, String name, TimeZone timeZone, TemporalAmount intervalLength) {
        return dataModel.getInstance(KpiImpl.class).init(name, timeZone, intervalLength);
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public List<? extends KpiMember> getMembers() {
        return Collections.unmodifiableList(members);
    }

    void doSave() {
        if (hasId()) {
            throw new IllegalStateException("The entity already persisted");
        }
        members.forEach(member -> {
            TimeSeries timeSeries = createKpiMemberTimeSeries();
            member.setTimeSeries(timeSeries);
        });
        dataModel.mapper(Kpi.class).persist(this);
    }

    void doUpdate() {
        if (!hasId()) {
            this.doSave();
            return;
        }
        members.stream().filter(not(IKpiMember::hasTimeSeries)).forEach(member -> {
            TimeSeries timeSeries = createKpiMemberTimeSeries();
            member.setTimeSeries(timeSeries);
            dataModel.mapper(IKpiMember.class).update(member, "timeSeries");
        });
        dataModel.mapper(Kpi.class).update(this);
    }

    private boolean hasId() {
        return id != 0L;
    }

    private TimeSeries createKpiMemberTimeSeries() {
        Vault vault = kpiService.getVault();
        RecordSpec recordSpec = kpiService.getRecordSpec();
        return vault.createRegularTimeSeries(recordSpec, getTimeZone(), getIntervalLength(), 0);
    }

    @Override
    public void remove() {
        if (hasId()) {
            members.clear();
            dataModel.mapper(Kpi.class).remove(this);
        }
    }

    public TimeZone getTimeZone() {
        if (timeZone == null) {
            timeZone = TimeZone.getTimeZone(members.get(0).getTimeSeries().getZoneId());
        }
        return timeZone;
    }

    @Override
    public TemporalAmount getIntervalLength() {
        if (intervalLength == null) {
            intervalLength = members.get(0).getTimeSeries().interval();
        }
        return intervalLength;
    }

    @Override
    public String getName() {
        return name;
    }

    KpiMemberImpl dynamicMaximum(String name) {
        return add(new KpiMemberImpl(idsService, eventService, dataModel, this, name).setDynamicTarget().asMaximum());
    }

    KpiMemberImpl dynamicMinimum(String name) {
        return add(new KpiMemberImpl(idsService, eventService, dataModel, this, name).setDynamicTarget().asMinimum());
    }

    KpiMemberImpl staticMaximum(String name, BigDecimal maximum) {
        return add(new KpiMemberImpl(idsService, eventService, dataModel, this, name).setStatictarget(maximum).asMaximum());
    }

    KpiMemberImpl staticMinimum(String name, BigDecimal minimum) {
        return add(new KpiMemberImpl(idsService, eventService, dataModel, this, name).setStatictarget(minimum).asMinimum());
    }

    KpiMemberImpl add(KpiMemberImpl member) {
        members.add(member);
        return member;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof KpiImpl)) {
            return false;
        }
        KpiImpl kpi = (KpiImpl) o;
        return Objects.equals(id, kpi.id);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public void store(Map<KpiMember, Map<Instant, BigDecimal>> memberScores) {
        TimeSeriesDataStorer storer = idsService.createOverrulingStorer();
        Map<IKpiMember, Range<Instant>> memberRangeMap = new HashMap<>();
        for (Map.Entry<KpiMember, Map<Instant, BigDecimal>> kpiMemberMapEntry : memberScores.entrySet()) {

            Optional<IKpiMember> iKpiMember = getIKpiMember(kpiMemberMapEntry.getKey());
            if (iKpiMember.isPresent()) {
                memberRangeMap.put(iKpiMember.get(), iKpiMember.get().addScores(storer, kpiMemberMapEntry.getValue()));
            } else {
                throw new IllegalArgumentException("Passed member " + kpiMemberMapEntry.getKey() + " is not a member of the kpi");
            }
        }
        storer.execute();
        memberRangeMap.forEach(IKpiMember::checkKpiScores);
    }

    private Optional<IKpiMember> getIKpiMember(KpiMember member) {
        return members.stream().filter(iKpiMember -> iKpiMember.equals(member)).findFirst();
    }

    @Override
    public KpiUpdater startUpdate() {
        return new KpiUpdaterImpl(this);
    }
}
