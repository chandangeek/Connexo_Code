package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataFormatter;
import com.elster.jupiter.export.ExportData;
import com.elster.jupiter.export.FormattedData;
import com.elster.jupiter.export.FormattedExportData;
import com.elster.jupiter.export.MeterReadingData;
import com.elster.jupiter.export.ReadingDataFormatter;
import com.elster.jupiter.export.ReadingTypeDataExportItem;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;

class DefaultItemExporter implements ItemExporter {

    private final ReadingDataFormatter dataFormatter;
    private final List<IReadingTypeDataExportItem> exportItems = new ArrayList<>();

    public DefaultItemExporter(DataFormatter dataFormatter) {
        if (dataFormatter instanceof ReadingDataFormatter) {
            this.dataFormatter = (ReadingDataFormatter) dataFormatter;
        } else {
            this.dataFormatter = new NoopFormatterDecorator(dataFormatter);
        }
    }

    private static class NoopFormatterDecorator implements ReadingDataFormatter {
        private final DataFormatter decorated;

        private NoopFormatterDecorator(DataFormatter decorated) {
            this.decorated = decorated;
        }

        @Override
        public void startExport(DataExportOccurrence occurrence, Logger logger) {
            decorated.startExport(occurrence, logger);
        }

        @Override
        public FormattedData processData(Stream<ExportData> data) {
            return decorated.processData(data);
        }

        @Override
        public void endExport() {
            decorated.endExport();
        }

        @Override
        public void startItem(ReadingTypeDataExportItem item) {
            // no op
        }

        @Override
        public void endItem(ReadingTypeDataExportItem item) {
            // no op
        }
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
