package com.elster.jupiter.export.rest.impl;


import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;

import javax.inject.Inject;
import java.util.List;

public class DataSourceInfoFactory {

    private final ReadingTypeInfoFactory readingTypeInfoFactory;

    @Inject
    public DataSourceInfoFactory(ReadingTypeInfoFactory readingTypeInfoFactory) {
        this.readingTypeInfoFactory = readingTypeInfoFactory;
    }

    public DataSourceInfos asInfoList(List<? extends ReadingTypeDataExportItem> exportItems) {
        DataSourceInfos infos = new DataSourceInfos();
        for (ReadingTypeDataExportItem item : exportItems) {
            infos.dataSources.add(this.asInfo(item));
            infos.total++;
        }
        return infos;
    }



    public DataSourceInfo asInfo(ReadingTypeDataExportItem item) {
        DataSourceInfo info = new DataSourceInfo();
        info.occurrenceId = item.getLastOccurrence().map(DataExportOccurrence::getId).orElse(null);

        item.getLastRun().ifPresent(instant -> {
            item.getReadingContainer().getMeter(instant)
                    .ifPresent(meter -> {
                        info.mRID = meter.getMRID();
                        info.serialNumber = meter.getSerialNumber();
                    });
        });
        info.readingType = readingTypeInfoFactory.from(item.getReadingType());
        item.getLastExportedDate().ifPresent(instant -> {
            info.lastExportedDate = instant.toEpochMilli();
        });
        return info;
    }
}
