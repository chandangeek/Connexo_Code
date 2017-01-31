/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kpi.impl;

import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.kpi.KpiEntry;
import com.elster.jupiter.kpi.KpiMember;

import java.math.BigDecimal;
import java.time.Instant;

public class KpiEntryImpl implements KpiEntry {

    private final KpiMember member;
    private final TimeSeriesEntry timeSeriesEntry;

    public KpiEntryImpl(KpiMember member, TimeSeriesEntry timeSeriesEntry) {
        this.member = member;
        this.timeSeriesEntry = timeSeriesEntry;
    }

    @Override
    public Instant getTimestamp() {
        return timeSeriesEntry.getTimeStamp();
    }

    @Override
    public BigDecimal getScore() {
        return timeSeriesEntry.getBigDecimal(0);
    }

    @Override
    public BigDecimal getTarget() {
        return member.hasDynamicTarget() ? timeSeriesEntry.getBigDecimal(1) : member.getTarget(getTimestamp());
    }

    @Override
    public boolean meetsTarget() {
        BigDecimal target = getTarget();
        if (target == null) {
            return true;
        }
        int compare = getScore().compareTo(target);
        return member.targetIsMinimum() ? compare >= 0 : compare <= 0;
    }
}
