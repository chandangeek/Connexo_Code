/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.impl.calc;

import com.elster.jupiter.dataquality.DeviceDataQualityKpi;
import com.elster.jupiter.dataquality.impl.DeviceDataQualityKpiImpl;
import com.elster.jupiter.tasks.TaskOccurrence;

import java.util.Optional;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum KpiType {

    DEVICE_DATA_QUALITY_KPI {
        @Override
        public String recurrentTaskName(String id) {
            return id + " - Device Data Quality KPI";
        }

        @Override
        public String recurrentTaskApplicationName() {
            return "MultiSense";
        }

        @Override
        DataQualityKpiCalculator newCalculator(long dataQualityKpiId, DataQualityServiceProvider serviceProvider, Logger logger) {
            Optional<DeviceDataQualityKpi> deviceDataQualityKpi = serviceProvider.dataQualityKpiService().findDeviceDataQualityKpi(dataQualityKpiId);
            if (deviceDataQualityKpi.isPresent()) {
                return new DeviceDataQualityKpiCalculator(serviceProvider, (DeviceDataQualityKpiImpl) deviceDataQualityKpi.get(), logger);
            } else {
                return new DataQualityKpiDoesNotExist(logger, "" + dataQualityKpiId);
            }
        }
    },
    USAGE_POINT_DATA_QUALITY_KPI {
        @Override
        public String recurrentTaskName(String id) {
            return id + " - Usage Point Data Quality KPI";
        }

        @Override
        public String recurrentTaskApplicationName() {
            return "Insight";
        }

        @Override
        DataQualityKpiCalculator newCalculator(long dataQualityKpiId, DataQualityServiceProvider serviceProvider, Logger logger) {
            throw new UnsupportedOperationException("Not implemented yet");
        }
    };

    private static final Logger LOGGER = Logger.getLogger(KpiType.class.getName());
    private static final Pattern PAYLOAD_PARSE_PATTERN = Pattern.compile("(\\w*)-(\\d*)");

    public abstract String recurrentTaskName(String id);

    public abstract String recurrentTaskApplicationName();

    public String recurrentPayload(long id) {
        return this.name() + '-' + id;
    }

    abstract DataQualityKpiCalculator newCalculator(long dataQualityKpiId, DataQualityServiceProvider serviceProvider, Logger logger);

    public static DataQualityKpiCalculator calculatorForRecurrentPayload(DataQualityServiceProvider serviceProvider, TaskOccurrence taskOccurrence, Logger logger) {
        String payload = taskOccurrence.getPayLoad();
        Matcher matcher = PAYLOAD_PARSE_PATTERN.matcher(payload);
        if (matcher.matches()) {
            KpiType kpiType = KpiType.valueOf(matcher.group(1));
            try {
                long id = Long.parseLong(matcher.group(2));
                return kpiType.newCalculator(id, serviceProvider, logger);
            } catch (NumberFormatException e) {
                return new PayloadContainsInvalidId(LOGGER, payload, e);
            }
        } else {
            return new UnexpectedPayloadFormat(LOGGER, payload);
        }
    }
}