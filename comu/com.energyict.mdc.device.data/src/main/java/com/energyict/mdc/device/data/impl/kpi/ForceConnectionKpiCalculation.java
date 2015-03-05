package com.energyict.mdc.device.data.impl.kpi;

import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;

import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Instant;
import java.util.logging.Logger;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-01-21 (13:55)
 */
@Component(name = "com.energyict.device.data.kpi.connection.force.calculation", service = ForceConnectionKpiCalculation.class, property = {"osgi.command.scope=mdcKPI", "osgi.command.function=calculateNow" }, immediate = true)
@SuppressWarnings("unused")
public class ForceConnectionKpiCalculation {

    private volatile TransactionService transactionService;
    private volatile ConnectionTaskService connectionTaskService;
    private volatile DataCollectionKpiService dataCollectionKpiService;
    private Logger logger = Logger.getLogger(ForceConnectionKpiCalculation.class.getName());

    @SuppressWarnings("unused")
    public void calculateNow(long dataCollectionKpiId) {
        this.dataCollectionKpiService
                .findDataCollectionKpi(dataCollectionKpiId)
                .map(DataCollectionKpiImpl.class::cast)
                .ifPresent(this::calculateNow);

    }

    private void calculateNow(DataCollectionKpiImpl dataCollectionKpi) {
        try (TransactionContext context = this.transactionService.getContext()) {
            new ConnectionSetupKpiCalculator(dataCollectionKpi, Instant.now(), this.connectionTaskService, this.logger).calculateAndStore();
        }
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setConnectionTaskService(ConnectionTaskService connectionTaskService) {
        this.connectionTaskService = connectionTaskService;
    }

    @Reference
    public void setDataCollectionKpiService(DataCollectionKpiService dataCollectionKpiService) {
        this.dataCollectionKpiService = dataCollectionKpiService;
    }

}