package com.energyict.mdc.device.data.impl.kpi;

import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.kpi.DataCollectionKpi;
import com.energyict.mdc.device.data.tasks.TaskStatus;

import com.elster.jupiter.kpi.Kpi;
import com.elster.jupiter.kpi.KpiMember;
import com.elster.jupiter.tasks.TaskExecutor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * Calculates the scores for a {@link DataCollectionKpiImpl}.
 * Todo: Execute the component from a scheduled job that uses
 *       the {@link DataCollectionKpiImpl}'s intervals for connection setup and com task execution.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-07 (16:50)
 */
public class DataCollectionKpiCalculator {

    private final Date timestamp;
    private final ConnectionTaskService connectionTaskService;
    private final CommunicationTaskService communicationTaskService;

    public DataCollectionKpiCalculator(Date timestamp, ConnectionTaskService connectionTaskService, CommunicationTaskService communicationTaskService) {
        super();
        this.timestamp = timestamp;
        this.connectionTaskService = connectionTaskService;
        this.communicationTaskService = communicationTaskService;
    }

    public static String expectedPayloadFor(DataCollectionKpiImpl kpi) {
        return String.valueOf(kpi.getId());
    }

    public void calculateAndStore(DataCollectionKpiImpl kpi) {
        if (kpi.calculatesConnectionSetupKpi()) {
            Map<TaskStatus, Long> statusCounters = this.connectionTaskService.getConnectionTaskStatusCount();
            this.calculateAndStore(kpi.connectionKpi().get(), statusCounters);
        }
        if (kpi.calculatesComTaskExecutionKpi()) {
            Map<TaskStatus, Long> statusCounters = this.communicationTaskService.getComTaskExecutionStatusCount();
            this.calculateAndStore(kpi.communicationKpi().get(), statusCounters);
        }
    }

    private void calculateAndStore(Kpi koreKpi, Map<TaskStatus, Long> statusCounters) {
        for (KpiMember kpiMember : koreKpi.getMembers()) {
            TaskStatus taskStatus = TaskStatus.valueOf(kpiMember.getName());
            Long counter = statusCounters.get(taskStatus);
            kpiMember.score(this.timestamp, new BigDecimal(counter));
        }
    }

}