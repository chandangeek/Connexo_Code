package com.energyict.mdc.device.data.impl.kpi;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.energyict.mdc.device.data.kpi.DataCollectionKpi;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;
import com.energyict.mdc.device.data.kpi.DataValidationKpi;
import com.energyict.mdc.device.data.kpi.DataValidationKpiService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskReportService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskReportService;

import java.util.Optional;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-15 (17:03)
 */
enum KpiType {

    VALIDATION {
        @Override
        public String recurrentTaskName(EndDeviceGroup deviceGroup) {
            return deviceGroup.getName() + " - Validation KPI";
        }

        @Override
        protected DataManagementKpiCalculator newCollectionCalculator(DataCollectionKpiImpl dataCollectionKpi, TaskOccurrence taskOccurrence, ServiceProvider serviceProvider) {
            return null;
        }

        @Override
        protected DataManagementKpiCalculator newValidationCalculator(DataValidationKpiImpl dataValidationKpi, TaskOccurrence taskOccurrence, ServiceProvider serviceProvider) {
            return new DataValidationKpiCalculator(dataValidationKpi, taskOccurrence.getTriggerTime(), LOGGER);
        }
    },

    CONNECTION {
        @Override
        public String recurrentTaskName(EndDeviceGroup deviceGroup) {
            return deviceGroup.getName() + " - Connection KPI";
        }

        @Override
        protected DataManagementKpiCalculator newCollectionCalculator(DataCollectionKpiImpl dataCollectionKpi, TaskOccurrence taskOccurrence, ServiceProvider serviceProvider) {
            return new ConnectionSetupKpiCalculator(dataCollectionKpi, taskOccurrence.getTriggerTime(), serviceProvider.connectionTaskReportService(), LOGGER);
        }

        @Override
        protected DataManagementKpiCalculator newValidationCalculator(DataValidationKpiImpl dataValidationKpi, TaskOccurrence taskOccurrence, ServiceProvider serviceProvider) {
            return null;
        }
    },

    COMMUNICATION {
        @Override
        public String recurrentTaskName(EndDeviceGroup deviceGroup) {
            return deviceGroup.getName() + " - Communication KPI";
        }

        @Override
        protected DataManagementKpiCalculator newCollectionCalculator(DataCollectionKpiImpl dataCollectionKpi, TaskOccurrence taskOccurrence, ServiceProvider serviceProvider) {
            return new CommunicationTaskExecutionKpiCalculator(dataCollectionKpi, taskOccurrence.getTriggerTime(), serviceProvider.communicationTaskService(), LOGGER);
        }

        @Override
        protected DataManagementKpiCalculator newValidationCalculator(DataValidationKpiImpl dataValidationKpi, TaskOccurrence taskOccurrence, ServiceProvider serviceProvider) {
            return null;
        }
    };

    public interface ServiceProvider {
        DataCollectionKpiService dataCollectionKpiService();
        DataValidationKpiService dataValidationKpiService();
        ConnectionTaskReportService connectionTaskReportService();
        CommunicationTaskReportService communicationTaskService();
    }

    private static final Logger LOGGER = Logger.getLogger(DataManagementKpiCalculatorHandler.class.getName());

    private static final Pattern PAYLOAD_PARSE_PATTERN = Pattern.compile("(\\w*)-(\\d*)");

    public abstract String recurrentTaskName(EndDeviceGroup endDeviceGroup);

    public String recurrentPayload(long id) {
        return this.name() + '-' + id;
    }

    protected abstract DataManagementKpiCalculator newCollectionCalculator(DataCollectionKpiImpl dataCollectionKpi, TaskOccurrence taskOccurrence, ServiceProvider serviceProvider);

    protected abstract DataManagementKpiCalculator newValidationCalculator(DataValidationKpiImpl dataValidationKpi, TaskOccurrence taskOccurrence, ServiceProvider serviceProvider);

    public static DataManagementKpiCalculator calculatorForRecurrentPayload(TaskOccurrence taskOccurrence, ServiceProvider serviceProvider) {
        String payload = taskOccurrence.getPayLoad();
        Matcher matcher = PAYLOAD_PARSE_PATTERN.matcher(payload);
        if (matcher.matches()) {
            KpiType kpiType = KpiType.valueOf(matcher.group(1));
            try {
                if(kpiType.equals(KpiType.COMMUNICATION) || kpiType.equals(KpiType.CONNECTION)) {
                    long id = Long.parseLong(matcher.group(2));
                    Optional<DataCollectionKpi> dataCollectionKpi = serviceProvider.dataCollectionKpiService()
                            .findDataCollectionKpi(id);
                    return dataCollectionKpi
                            .map(DataCollectionKpiImpl.class::cast)
                            .map(kpi -> kpiType.newCollectionCalculator(kpi, taskOccurrence, serviceProvider))
                            .orElseGet(() -> new DataManagementKpiDoesNotExist(LOGGER, payload));
                } else {
                    long id = Long.parseLong(matcher.group(2));
                    Optional<DataValidationKpi> dataValidationKpi = serviceProvider.dataValidationKpiService().findDataValidationKpi(id);
                    return dataValidationKpi
                            .map(DataValidationKpiImpl.class::cast)
                            .map(kpi -> kpiType.newValidationCalculator(kpi, taskOccurrence, serviceProvider))
                            .orElseGet(() -> new DataManagementKpiDoesNotExist(LOGGER, payload));
                }
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