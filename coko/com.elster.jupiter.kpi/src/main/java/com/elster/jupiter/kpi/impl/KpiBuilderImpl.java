/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kpi.impl;

import com.elster.jupiter.kpi.Kpi;
import com.elster.jupiter.kpi.KpiBuilder;
import com.elster.jupiter.orm.DataModel;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

class KpiBuilderImpl implements KpiBuilder {

    private String name;
    private List<KpiMemberBuilderImpl> members = new ArrayList<>();
    private TimeZone timeZone = TimeZone.getTimeZone("UTC");
    private TemporalAmount intervalLength;

    private final DataModel dataModel;

    KpiBuilderImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public Kpi create() {
        KpiImpl kpi = KpiImpl.from(dataModel, name, timeZone, intervalLength);
        for (KpiMemberBuilderImpl member : members) {
            member.create(kpi);
        }
        kpi.doSave();
        return kpi;
    }

    @Override
    public KpiBuilder named(String name) {
        this.name = name;
        return this;
    }

    @Override
    public KpiBuilder timeZone(TimeZone zone) {
        this.timeZone = zone;
        return this;
    }

    @Override
    public KpiBuilder timeZone(ZoneId zone) {
        this.timeZone = TimeZone.getTimeZone(zone);
        return this;
    }

    @Override
    public KpiBuilder interval(TemporalAmount interval) {
        this.intervalLength = interval;
        return this;
    }

    @Override
    public KpiMemberBuilder member() {
        return new KpiMemberBuilderImpl();
    }

    private class KpiMemberBuilderImpl implements KpiMemberBuilder {

        private BigDecimal targetValue;
        private boolean dynamic;
        private boolean targetIsMinimum;
        private String name;

        @Override
        public KpiMemberBuilder withTargetSetAt(BigDecimal target) {
            this.targetValue = target;
            return this;
        }

        @Override
        public KpiMemberBuilder withDynamicTarget() {
            this.dynamic = true;
            return this;
        }

        @Override
        public KpiMemberBuilder asMinimum() {
            this.targetIsMinimum = true;
            return this;
        }

        @Override
        public KpiMemberBuilder asMaximum() {
            this.targetIsMinimum = false;
            return this;
        }

        @Override
        public KpiMemberBuilder named(String name) {
            this.name = name;
            return this;
        }

        @Override
        public KpiBuilder add() {
            KpiBuilderImpl.this.members.add(this);
            return KpiBuilderImpl.this;
        }

        KpiMemberImpl create(KpiImpl kpi) {
            if (dynamic) {
                if (targetIsMinimum) {
                    return kpi.dynamicMinimum(name);
                }
                return kpi.dynamicMaximum(name);
            }
            if (targetIsMinimum) {
                return kpi.staticMinimum(name, targetValue);
            }
            return kpi.staticMaximum(name, targetValue);
        }
    }
}
