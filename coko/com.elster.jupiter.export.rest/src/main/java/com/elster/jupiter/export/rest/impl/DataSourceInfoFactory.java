package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;

import javax.inject.Inject;
import java.time.Instant;

public class DataSourceInfoFactory {

    private final ReadingTypeInfoFactory readingTypeInfoFactory;

    @Inject
    public DataSourceInfoFactory(ReadingTypeInfoFactory readingTypeInfoFactory) {
        this.readingTypeInfoFactory = readingTypeInfoFactory;
    }

    public DataSourceInfo asInfo(ReadingTypeDataExportItem item) {
        DataSourceInfo info = new DataSourceInfo();
        info.occurrenceId = item.getLastOccurrence().map(DataExportOccurrence::getId).orElse(null);
        item.getLastRun().ifPresent(instant ->
                info.details = getDataSourceDetails(item.getReadingContainer(), instant)
        );
        info.readingType = readingTypeInfoFactory.from(item.getReadingType());
        item.getLastExportedDate().ifPresent(instant -> info.lastExportedDate = instant);
        return info;
    }

    private DataSourceInfo.DataSource getDataSourceDetails(ReadingContainer readingContainer, Instant instant) {
        if (readingContainer instanceof Meter) {
            return readingContainer.getMeter(instant).map(this::asDataSource).orElse(null);
        } else {
            return readingContainer.getUsagePoint(instant).map(this::asDataSource).orElse(null);
        }
    }

    private DataSourceInfo.MeterDataSource asDataSource(Meter meter) {
        DataSourceInfo.MeterDataSource meterDataSource = new DataSourceInfo.MeterDataSource();
        meterDataSource.name = meter.getName();
        meterDataSource.serialNumber = meter.getSerialNumber();
        return meterDataSource;
    }

    private DataSourceInfo.UsagePointDataSource asDataSource(UsagePoint usagePoint) {
        DataSourceInfo.UsagePointDataSource usagePointDataSource = new DataSourceInfo.UsagePointDataSource();
        usagePointDataSource.name = usagePoint.getName();
        usagePointDataSource.connectionState = usagePoint.getConnectionStateDisplayName();
        return usagePointDataSource;
    }
}
