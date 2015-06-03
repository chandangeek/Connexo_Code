package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataFormatter;
import com.elster.jupiter.export.FormattedData;
import com.elster.jupiter.export.FormattedExportData;
import com.elster.jupiter.export.MeterReadingData;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

class DefaultItemExporter implements ItemExporter {

    private final DataFormatter dataFormatter;
    private final List<IReadingTypeDataExportItem> exportItems = new ArrayList<>();

    public DefaultItemExporter(DataFormatter dataFormatter) {
        this.dataFormatter = dataFormatter;
    }

    @Override
    public List<FormattedExportData> exportItem(DataExportOccurrence occurrence, MeterReadingData meterReadingData) {
        IReadingTypeDataExportItem item = (IReadingTypeDataExportItem) meterReadingData.getItem();
        dataFormatter.startItem(item);
        item.setLastRun(occurrence.getTriggerTime());
        FormattedData formattedData = dataFormatter.processData(Stream.of(meterReadingData));
        Optional<Instant> lastExported = formattedData.lastExported();
        lastExported.ifPresent(item::setLastExportedDate);
        dataFormatter.endItem(item);
        exportItems.add(item);
        return formattedData.getData();
    }

    @Override
    public void done() {
        exportItems.forEach(IReadingTypeDataExportItem::update);
    }

}
