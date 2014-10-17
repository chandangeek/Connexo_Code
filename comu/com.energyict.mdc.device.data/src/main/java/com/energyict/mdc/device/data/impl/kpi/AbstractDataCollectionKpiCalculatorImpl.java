package com.energyict.mdc.device.data.impl.kpi;

import com.energyict.mdc.device.data.tasks.TaskStatus;

import com.elster.jupiter.kpi.Kpi;
import com.elster.jupiter.kpi.KpiMember;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * Provides code reuse opportunities for classes that
 * implement the {@link DataCollectionKpiCalculator} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-15 (17:48)
 */
public class AbstractDataCollectionKpiCalculatorImpl {

    private final Date timestamp;

    public AbstractDataCollectionKpiCalculatorImpl(Date timestamp) {
        super();
        this.timestamp = timestamp;
    }

    protected void calculateAndStore(Kpi koreKpi, Map<TaskStatus, Long> statusCounters) {
        for (KpiMember kpiMember : koreKpi.getMembers()) {
            MonitoredTaskStatus taskStatus = MonitoredTaskStatus.valueOf(kpiMember.getName());
            long counter = taskStatus.calculateFrom(statusCounters);
            kpiMember.score(this.timestamp.toInstant(), new BigDecimal(counter));
        }
    }

}