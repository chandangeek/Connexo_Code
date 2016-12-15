package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.UsagePoint;
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
        info.details = getDataSourceDetails(item.getDomainObject());
        item.getLastExportedDate().ifPresent(instant -> info.lastExportedDate = instant);
        return info;
    }

    private DataSourceInfo.DataSource getDataSourceDetails(IdentifiedObject domainObject) {
        if (domainObject instanceof Meter) {
            return asDataSource((Meter) domainObject);
        } else if (domainObject instanceof UsagePoint) {
            return asDataSource((UsagePoint) domainObject);
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

    private DataSourceInfo.UsagePointDataSource asDataSource(UsagePoint usagePoint) {
        DataSourceInfo.UsagePointDataSource usagePointDataSource = new DataSourceInfo.UsagePointDataSource();
        usagePointDataSource.name = usagePoint.getName();
        if (usagePoint.getCurrentConnectionState().isPresent()) {
            usagePointDataSource.connectionState = usagePoint.getConnectionStateDisplayName();
        }
        return usagePointDataSource;
    }
}
