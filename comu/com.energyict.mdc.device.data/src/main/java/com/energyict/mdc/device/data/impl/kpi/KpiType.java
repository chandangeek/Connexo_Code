package com.energyict.mdc.device.data.impl.kpi;

import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.kpi.DataCollectionKpi;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;

import com.elster.jupiter.tasks.TaskOccurrence;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-15 (17:03)
 */
public enum KpiType {

    CONNECTION {
        @Override
        public String recurrentTaskNamePattern() {
            return "ConnectionKpiCalculator({0})";
        }

        @Override
        protected DataCollectionKpiCalculator newCalculator(DataCollectionKpiImpl dataCollectionKpi, TaskOccurrence taskOccurrence, ServiceProvider serviceProvider) {
            return new ConnectionSetupKpiCalculator(dataCollectionKpi, taskOccurrence.getTriggerTime(), serviceProvider.connectionTaskService(), LOGGER);
        }
    },

    COMMUNICATION {
        @Override
        public String recurrentTaskNamePattern() {
            return "CommunicationKpiCalculator({0})";
        }

        @Override
        protected DataCollectionKpiCalculator newCalculator(DataCollectionKpiImpl dataCollectionKpi, TaskOccurrence taskOccurrence, ServiceProvider serviceProvider) {
            return new CommunicationTaskExecutionKpiCalculator(dataCollectionKpi, taskOccurrence.getTriggerTime(), serviceProvider.communicationTaskService(), LOGGER);
        }
    };

    public interface ServiceProvider {
        public DataCollectionKpiService dataCollectionKpiService();
        public ConnectionTaskService connectionTaskService();
        public CommunicationTaskService communicationTaskService();
    }

    private static final Logger LOGGER = Logger.getLogger(DataCollectionKpiCalculatorHandler.class.getName());

    private static final Pattern PAYLOAD_PARSE_PATTERN = Pattern.compile("(\\w*)-(\\d*)");
    public abstract String recurrentTaskNamePattern();

    public String recurrentTaskName() {
        return MessageFormat.format(this.recurrentTaskNamePattern(), UUID.randomUUID());
    }

    public String recurrentPayload(DataCollectionKpiImpl dataCollectionKpi) {
        return this.name() + '-' + dataCollectionKpi.getId();
    }

    protected abstract DataCollectionKpiCalculator newCalculator(DataCollectionKpiImpl dataCollectionKpi, TaskOccurrence taskOccurrence, ServiceProvider serviceProvider);

    public static DataCollectionKpiCalculator calculatorForRecurrentPayload(TaskOccurrence taskOccurrence, ServiceProvider serviceProvider) {
        String payload = taskOccurrence.getPayLoad();
        Matcher matcher = PAYLOAD_PARSE_PATTERN.matcher(payload);
        if (matcher.matches()) {
            KpiType kpiType = KpiType.valueOf(matcher.group(1));
            try {
                long id = Long.parseLong(matcher.group(2));
                Optional<DataCollectionKpi> dataCollectionKpi = serviceProvider.dataCollectionKpiService().findDataCollectionKpi(id);
                return dataCollectionKpi
                            .map(DataCollectionKpiImpl.class::cast)
                            .map(kpi -> kpiType.newCalculator(kpi, taskOccurrence, serviceProvider))
                            .orElseGet(() -> new DataCollectionKpiDoesNotExist(LOGGER, payload));
            }
            catch (NumberFormatException e) {
                return new PayloadContainsInvalidId(LOGGER, payload, e);
            }
        }
        else {
            return new UnexpectedPayloadFormat(LOGGER, payload);
        }
    }

}