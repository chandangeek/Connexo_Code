/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.processor.impl;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.metering.MetrologyContractChannelsContainer;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.validation.ValidationResult;

import javax.inject.Inject;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

public class CsvUsagePointDataFormatter extends StandardCsvDataFormatter{

    private MetrologyPurpose metrologyPurpose;

    @Inject
    public CsvUsagePointDataFormatter(DataExportService dataExportService) {
        super(dataExportService);
    }

    CsvUsagePointDataFormatter(Map<String, Object> propertyMap, DataExportService dataExportService) {
        super(propertyMap, dataExportService);
    }

    @Override
    public void startItem(ReadingTypeDataExportItem item) {
        super.startItem(item);
        metrologyPurpose = ((MetrologyContractChannelsContainer)item.getReadingContainer()).getMetrologyContract().getMetrologyPurpose();
    }

    @Override
    Optional<String> writeReading(BaseReading reading, ValidationResult validationResult) {
        if (reading.getValue() != null || reading instanceof Reading && ((Reading) reading).getText() != null) {
            ZonedDateTime date = ZonedDateTime.ofInstant(reading.getTimeStamp(), ZoneId.systemDefault());
            StringJoiner joiner = new StringJoiner(fieldSeparator, "", "\n")
                    .add(DEFAULT_DATE_TIME_FORMAT.format(date))
                    .add(domainObject.getMRID())
                    .add(domainObject.getName())
                    .add(metrologyPurpose.getName())
                    .add(readingType.getMRID())
                    .add(reading.getValue() != null ? reading.getValue().toString() : ((Reading) reading).getText())
                    .add(asString(validationResult));
            return Optional.of(joiner.toString());
        }
        return Optional.empty();
    }
}
