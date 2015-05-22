package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataSelector;
import com.elster.jupiter.export.ExportData;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.groups.EndDeviceMembership;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.streams.Functions;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReadingTypeDataSelector implements DataSelector {

    private final TransactionService transactionService;
    private final Logger logger;

    public ReadingTypeDataSelector(TransactionService transactionService, Logger logger) {
        this.transactionService = transactionService;
        this.logger = logger;
    }

    @Override
    public Stream<ExportData> selectData(DataExportOccurrence occurrence) {
        IReadingTypeExportTask task = (IReadingTypeExportTask) occurrence.getTask();
        Set<IReadingTypeDataExportItem> activeItems;
        try (TransactionContext context = transactionService.getContext()) {
            activeItems = getActiveItems(task, occurrence);

            task.getExportItems().stream()
                    .filter(item -> !activeItems.contains(item))
                    .peek(IReadingTypeDataExportItem::deactivate)
                    .forEach(IReadingTypeDataExportItem::update);
            activeItems.stream()
                    .peek(IReadingTypeDataExportItem::activate)
                    .forEach(IReadingTypeDataExportItem::update);
            context.commit();
        }

        return activeItems.stream()
                .map(item -> DefaultItemDataSelector.INSTANCE.selectData(occurrence, item))
                .flatMap(Functions.asStream());
    }

    private Set<IReadingTypeDataExportItem> getActiveItems(IReadingTypeExportTask task, DataExportOccurrence occurrence) {
        return task.getEndDeviceGroup().getMembers(occurrence.getExportedDataInterval()).stream()
                .map(EndDeviceMembership::getEndDevice)
                .filter(device -> device instanceof Meter)
                .map(Meter.class::cast)
                .flatMap(meter -> readingTypeDataExportItems(task, meter))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Stream<IReadingTypeDataExportItem> readingTypeDataExportItems(IReadingTypeExportTask task, Meter meter) {
        return task.getReadingTypes().stream()
                .map(r -> task.getExportItems().stream()
                                .map(IReadingTypeDataExportItem.class::cast)
                                .filter(item -> r.equals(item.getReadingType()))
                                .filter(i -> i.getReadingContainer().is(meter))
                                .findAny()
                                .orElseGet(() -> task.addExportItem(meter, r))
                );
    }

}
