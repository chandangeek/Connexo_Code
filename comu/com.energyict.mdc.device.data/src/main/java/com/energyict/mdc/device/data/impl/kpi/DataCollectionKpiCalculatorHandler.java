package com.energyict.mdc.device.data.impl.kpi;

import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.kpi.DataCollectionKpi;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;

import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Calculates the scores for a {@link DataCollectionKpiImpl}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-07 (16:50)
 */
public class DataCollectionKpiCalculatorHandler implements TaskExecutor {

    private static final Logger LOGGER = Logger.getLogger(DataCollectionKpiCalculatorHandler.class.getName());

    private final DataCollectionKpiService dataCollectionKpiService;
    private final ConnectionTaskService connectionTaskService;
    private final CommunicationTaskService communicationTaskService;

    public DataCollectionKpiCalculatorHandler(DataCollectionKpiService dataCollectionKpiService, ConnectionTaskService connectionTaskService, CommunicationTaskService communicationTaskService) {
        super();
        this.dataCollectionKpiService = dataCollectionKpiService;
        this.connectionTaskService = connectionTaskService;
        this.communicationTaskService = communicationTaskService;
    }

    @Override
    public void execute(TaskOccurrence taskOccurrence) {
        try {
            Long id = Long.valueOf(taskOccurrence.getPayLoad());
            Optional<DataCollectionKpi> dataCollectionKpi = this.dataCollectionKpiService.findDataCollectionKpi(id);
            if (dataCollectionKpi.isPresent()) {
                DataCollectionKpiCalculator calculator = new DataCollectionKpiCalculator(taskOccurrence.getTriggerTime(), this.connectionTaskService, this.communicationTaskService);
                calculator.calculateAndStore((DataCollectionKpiImpl) dataCollectionKpi.get());
            }
            else {
                LOGGER.log(Level.SEVERE, "Payload '" + taskOccurrence.getPayLoad() + "' is not the unique identifier of a " + DataCollectionKpi.class.getSimpleName());
            }
        }
        catch (NumberFormatException e) {
            LOGGER.log(Level.SEVERE, "Payload '" + taskOccurrence.getPayLoad() + "' cannot be parsed to Long", e);
        }
    }

}