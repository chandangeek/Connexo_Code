/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.kpi;

import com.energyict.mdc.device.data.tasks.ConnectionTaskReportService;
import com.energyict.mdc.device.data.tasks.TaskStatus;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Calculates the scores of the connection setup KPI for a {@link DataCollectionKpiImpl}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-07 (16:50)
 */
public class ConnectionSetupKpiCalculator extends AbstractDataCollectionKpiCalculatorImpl implements DataCollectionKpiCalculator {

    private final DataCollectionKpiImpl kpi;
    private final ConnectionTaskReportService connectionTaskReportService;
    private final Logger logger;

    public ConnectionSetupKpiCalculator(DataCollectionKpiImpl dataCollectionKpi, Instant timestamp, ConnectionTaskReportService connectionTaskReportService, Logger logger) {
        super(timestamp);
        this.kpi = dataCollectionKpi;
        this.connectionTaskReportService = connectionTaskReportService;
        this.logger = logger;
    }

    public void calculateAndStore() {
        if (this.kpi.calculatesConnectionSetupKpi()) {
            Map<TaskStatus, Long> statusCounters = this.connectionTaskReportService.getConnectionTaskStatusCount();
            long total = EnumSet.complementOf(EnumSet.of(TaskStatus.OnHold)).stream().mapToLong(statusCounters::get).sum();
            if (total > 0) {
                this.calculateAndStore(this.kpi.connectionKpi().get(), statusCounters, total);
            }
            else {
                this.logger.fine(() -> "Ignoring calculated result of DataCollectionKpi (id=" + this.kpi.getId() + ") because total number of connections is zero.");
            }
        }
        else {
            this.logger.severe(() -> ConnectionSetupKpiCalculator.class.getSimpleName() + " was executed for DataCollectionKpi (id=" + this.kpi.getId() + ") but that is not configured for connection setup");
        }
    }

}