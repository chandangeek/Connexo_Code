package com.energyict.mdc.device.data.impl.kpi;

import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.kpi.DataCollectionKpi;
import com.energyict.mdc.device.data.tasks.TaskStatus;

import java.util.Date;
import java.util.Map;

/**
 * Calculates the scores of the connection setup KPI for a {@link DataCollectionKpiImpl}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-07 (16:50)
 */
public class ConnectionSetupKpiCalculator extends AbstractDataCollectionKpiCalculatorImpl implements DataCollectionKpiCalculator {

    private final DataCollectionKpiImpl kpi;
    private final ConnectionTaskService connectionTaskService;

    public ConnectionSetupKpiCalculator(DataCollectionKpiImpl dataCollectionKpi, Date timestamp, ConnectionTaskService connectionTaskService) {
        super(timestamp);
        this.kpi = dataCollectionKpi;
        this.connectionTaskService = connectionTaskService;
    }

    public void calculateAndStore() {
        if (this.kpi.calculatesConnectionSetupKpi()) {
            Map<TaskStatus, Long> statusCounters = this.connectionTaskService.getConnectionTaskStatusCount();
            this.calculateAndStore(this.kpi.connectionKpi().get(), statusCounters);
        }
    }

}