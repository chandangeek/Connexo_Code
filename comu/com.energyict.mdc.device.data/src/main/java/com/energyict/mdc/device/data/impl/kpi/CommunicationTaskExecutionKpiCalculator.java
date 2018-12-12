/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.kpi;

import com.energyict.mdc.device.data.tasks.CommunicationTaskReportService;
import com.energyict.mdc.device.data.tasks.TaskStatus;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Calculates the scores of the communication task execution KPI for a {@link DataCollectionKpiImpl}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-07 (16:50)
 */
public class CommunicationTaskExecutionKpiCalculator extends AbstractDataCollectionKpiCalculatorImpl implements DataCollectionKpiCalculator {

    private final DataCollectionKpiImpl kpi;
    private final CommunicationTaskReportService communicationTaskReportService;
    private final Logger logger;

    public CommunicationTaskExecutionKpiCalculator(DataCollectionKpiImpl dataCollectionKpi, Instant timestamp, CommunicationTaskReportService communicationTaskReportService, Logger logger) {
        super(timestamp);
        this.kpi = dataCollectionKpi;
        this.communicationTaskReportService = communicationTaskReportService;
        this.logger = logger;
    }

    public void calculateAndStore() {
        if (this.kpi.calculatesComTaskExecutionKpi()) {
            Map<TaskStatus, Long> statusCounters = this.communicationTaskReportService.getComTaskExecutionStatusCount();
            long total = EnumSet.complementOf(EnumSet.of(TaskStatus.OnHold)).stream().mapToLong(statusCounters::get).sum();
            if (total > 0) {
                this.calculateAndStore(this.kpi.communicationKpi().get(), statusCounters, total);
            }
            else {
                this.logger.fine(() -> "Ignoring calculated result of DataCollectionKpi (id=" + this.kpi.getId() + ") because total number of communication tasks is zero.");
            }
        }
        else {
            this.logger.severe(CommunicationTaskExecutionKpiCalculator.class.getSimpleName() + " was executed for DataCollectionKpi (id=" + this.kpi.getId() + ") but that is not configured for communication task execution");
        }
    }

}