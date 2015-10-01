package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataSelector;
import com.elster.jupiter.export.DefaultSelectorOccurrence;
import com.elster.jupiter.export.ExportData;
import com.elster.jupiter.export.MeterEventData;
import com.elster.jupiter.metering.groups.EndDeviceMembership;
import com.elster.jupiter.metering.readings.beans.EndDeviceEventImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.orm.DataModel;
import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Instant;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class AsEventDataSelector implements DataSelector {

    private final DataExportService dataExportService;

    private ReadingTypeDataSelectorImpl selector;
    private Logger logger;

    @Inject
    AsEventDataSelector(DataExportService dataExportService) {
        this.dataExportService = dataExportService;
    }

    static DataSelector from(DataModel dataModel, ReadingTypeDataSelectorImpl readingTypeDataSelector, Logger logger) {
        return dataModel.getInstance(AsEventDataSelector.class).init(readingTypeDataSelector, logger);
    }

    private AsEventDataSelector init(ReadingTypeDataSelectorImpl selector, Logger logger) {
        this.selector = selector;
        this.logger = logger;
        return this;
    }

    @Override
    public Stream<ExportData> selectData(DataExportOccurrence occurrence) {
        Range<Instant> range = occurrence.getDefaultSelectorOccurrence()
                .map(DefaultSelectorOccurrence::getExportedDataInterval)
                .orElse(Range.<Instant>all());
        return selector.getEndDeviceGroup()
                .getMembers(range)
                .stream()
                .map(EndDeviceMembership::getEndDevice)
                .map(endDevice ->  {
                    MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
                    meterReading.addAllEndDeviceEvents(endDevice.getDeviceEvents(range).stream()
                            .map(endDeviceEventRecord -> EndDeviceEventImpl.of(endDeviceEventRecord.getMRID(), endDeviceEventRecord.getCreatedDateTime()))
                            .collect(Collectors.toList()));
                    return new MeterEventData(meterReading, dataExportService.forRoot(endDevice.getMRID()));
                });
    }
}
