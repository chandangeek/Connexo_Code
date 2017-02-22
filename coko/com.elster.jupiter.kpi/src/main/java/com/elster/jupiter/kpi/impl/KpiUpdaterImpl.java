/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kpi.impl;

import com.elster.jupiter.kpi.Kpi;
import com.elster.jupiter.kpi.KpiUpdater;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

class KpiUpdaterImpl implements KpiUpdater {

    private final KpiImpl kpi;
    private final List<KpiMemberBuilderImpl> members = new ArrayList<>();

    public KpiUpdaterImpl(KpiImpl kpi) {
        this.kpi = kpi;
    }

    @Override
    public KpiMemberBuilder member() {
        return new KpiMemberBuilderImpl();
    }

    @Override
    public Kpi update() {
        members.forEach(member -> member.create(kpi));
        kpi.doUpdate();
        return kpi;
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
        public KpiUpdater add() {
            KpiUpdaterImpl.this.members.add(this);
            return KpiUpdaterImpl.this;
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
