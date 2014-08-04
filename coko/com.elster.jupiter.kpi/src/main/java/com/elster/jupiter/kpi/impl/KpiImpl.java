package com.elster.jupiter.kpi.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.IntervalLength;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.kpi.Kpi;
import com.elster.jupiter.kpi.KpiMember;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

class KpiImpl implements Kpi {

    private long id;
    private String name;
    private List<IKpiMember> members = new ArrayList<>();

    private transient TimeZone timeZone;
    private transient IntervalLength intervalLength;

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

    KpiImpl init(String name, TimeZone timeZone, IntervalLength intervalLength) {
        this.name = name;
        this.timeZone = timeZone;
        this.intervalLength = intervalLength;
        return this;
    }

    static KpiImpl from(DataModel dataModel, String name, TimeZone timeZone, IntervalLength intervalLength) {
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
        if (id == 0L) {
            Vault vault = kpiService.getVault();
            RecordSpec recordSpec = kpiService.getRecordSpec();
            for (IKpiMember member : members) {
                TimeSeries timeSeries = vault.createRegularTimeSeries(recordSpec, getTimeZone(), getIntervalLength(), 0);
                member.setTimeSeries(timeSeries);
            }
            dataModel.mapper(Kpi.class).persist(this);
        } else {
            dataModel.mapper(Kpi.class).update(this);
        }
    }

    public TimeZone getTimeZone() {
        if (timeZone == null) {
            timeZone = members.get(0).getTimeSeries().getTimeZone();
        }
        return timeZone;
    }

    @Override
    public IntervalLength getIntervalLength() {
        if (intervalLength == null) {
            intervalLength = members.get(0).getTimeSeries().getIntervalLength();
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
