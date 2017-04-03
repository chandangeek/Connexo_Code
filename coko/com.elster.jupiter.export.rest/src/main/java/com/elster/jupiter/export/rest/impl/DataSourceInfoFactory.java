/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MetrologyContractChannelsContainer;
import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;

import javax.inject.Inject;

public class DataSourceInfoFactory {

    private final ReadingTypeInfoFactory readingTypeInfoFactory;

    @Inject
    public DataSourceInfoFactory(ReadingTypeInfoFactory readingTypeInfoFactory) {
        this.readingTypeInfoFactory = readingTypeInfoFactory;
    }

    public DataSourceInfo asInfo(ReadingTypeDataExportItem item) {
        DataSourceInfo info = new DataSourceInfo();
        info.occurrenceId = item.getLastOccurrence().map(DataExportOccurrence::getId).orElse(null);
        info.readingType = readingTypeInfoFactory.from(item.getReadingType());
        info.details = getDataSourceDetails(item.getReadingContainer());
        item.getLastExportedDate().ifPresent(instant -> info.lastExportedDate = instant);
        return info;
    }

    private DataSourceInfo.DataSource getDataSourceDetails(ReadingContainer readingContainer) {
        if (readingContainer instanceof Meter) {
            return asDataSource((Meter) readingContainer);
        } else if (readingContainer instanceof MetrologyContractChannelsContainer) {
            return asDataSource((MetrologyContractChannelsContainer) readingContainer);
        } else {
            return null;
        }
    }

    private DataSourceInfo.MeterDataSource asDataSource(Meter meter) {
        DataSourceInfo.MeterDataSource meterDataSource = new DataSourceInfo.MeterDataSource();
        meterDataSource.name = meter.getName();
        meterDataSource.serialNumber = meter.getSerialNumber();
        return meterDataSource;
    }

    private DataSourceInfo.UsagePointDataSource asDataSource(MetrologyContractChannelsContainer channelsContainer) {
        DataSourceInfo.UsagePointDataSource usagePointDataSource = new DataSourceInfo.UsagePointDataSource();
        usagePointDataSource.name = channelsContainer.getUsagePoint().get().getName();
        usagePointDataSource.purpose = channelsContainer.getMetrologyContract().getMetrologyPurpose().getName();
        return usagePointDataSource;
    }
}
