/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.kpi;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.energyict.mdc.device.data.kpi.DataCollectionKpi;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;
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

    CONNECTION {
        @Override
        public String recurrentTaskName(EndDeviceGroup deviceGroup) {
            return deviceGroup.getName() + " - Connection KPI";
        }

        @Override
        protected DataCollectionKpiCalculator newCalculator(DataCollectionKpiImpl dataCollectionKpi, TaskOccurrence taskOccurrence, ServiceProvider serviceProvider) {
            return new ConnectionSetupKpiCalculator(dataCollectionKpi, taskOccurrence.getTriggerTime(), serviceProvider.connectionTaskReportService(), LOGGER);
        }
    },

    COMMUNICATION {
        @Override
        public String recurrentTaskName(EndDeviceGroup deviceGroup) {
            return deviceGroup.getName() + " - Communication KPI";
        }

        @Override
        protected DataCollectionKpiCalculator newCalculator(DataCollectionKpiImpl dataCollectionKpi, TaskOccurrence taskOccurrence, ServiceProvider serviceProvider) {
            return new CommunicationTaskExecutionKpiCalculator(dataCollectionKpi, taskOccurrence.getTriggerTime(), serviceProvider.communicationTaskService(), LOGGER);
        }
    };

    public interface ServiceProvider {
        DataCollectionKpiService dataCollectionKpiService();
        ConnectionTaskReportService connectionTaskReportService();
        CommunicationTaskReportService communicationTaskService();
    }

    private static final Logger LOGGER = Logger.getLogger(DataCollectionKpiCalculatorHandler.class.getName());

    private static final Pattern PAYLOAD_PARSE_PATTERN = Pattern.compile("(\\w*)-(\\d*)");

    public abstract String recurrentTaskName(EndDeviceGroup endDeviceGroup);

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