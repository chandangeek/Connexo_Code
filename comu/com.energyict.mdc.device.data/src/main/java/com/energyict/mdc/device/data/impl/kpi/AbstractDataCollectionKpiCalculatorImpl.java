/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.kpi;

import com.elster.jupiter.kpi.Kpi;
import com.elster.jupiter.kpi.KpiMember;
import com.energyict.mdc.device.data.tasks.TaskStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

/**
 * Provides code reuse opportunities for classes that
 * implement the {@link DataCollectionKpiCalculator} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-15 (17:48)
 */
public class AbstractDataCollectionKpiCalculatorImpl {

    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private final Instant timestamp;

    public AbstractDataCollectionKpiCalculatorImpl(Instant timestamp) {
        super();
        this.timestamp = timestamp;
    }

    protected void calculateAndStore(Kpi koreKpi, Map<TaskStatus, Long> statusCounters, long total) {
        for (KpiMember kpiMember : koreKpi.getMembers()) {
            MonitoredTaskStatus taskStatus = MonitoredTaskStatus.valueOf(kpiMember.getName());
            long counter = taskStatus.calculateFrom(statusCounters);
            kpiMember.score(this.timestamp, HUNDRED.multiply(new BigDecimal(counter)).divide(new BigDecimal(total), BigDecimal.ROUND_HALF_UP));
        }
    }

}