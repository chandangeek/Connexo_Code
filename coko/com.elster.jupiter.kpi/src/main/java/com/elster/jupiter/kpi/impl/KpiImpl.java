package com.elster.jupiter.kpi.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.kpi.Kpi;
import com.elster.jupiter.kpi.KpiMember;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;

import java.math.BigDecimal;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

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

    @Override
    public void save() {
        if (hasId()) {
            dataModel.mapper(Kpi.class).update(this);
            return;
        }
        Vault vault = kpiService.getVault();
        RecordSpec recordSpec = kpiService.getRecordSpec();
        members.forEach(member -> {
            TimeSeries timeSeries = vault.createRegularTimeSeries(recordSpec, getTimeZone(), getIntervalLength(), 0);
            member.setTimeSeries(timeSeries);
        });
        dataModel.mapper(Kpi.class).persist(this);
    }

    private boolean hasId() {
        return id != 0L;
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

}
