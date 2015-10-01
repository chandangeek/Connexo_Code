package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataSelector;
import com.elster.jupiter.export.ExportData;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.streams.Functions;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class AsReadingTypeDataSelector implements DataSelector {
    private final DataModel dataModel;
    private final TransactionService transactionService;

    private Logger logger;
    private ReadingTypeDataSelectorImpl selector;

    @Inject
    AsReadingTypeDataSelector(DataModel dataModel, TransactionService transactionService) {
        this.dataModel = dataModel;
        this.transactionService = transactionService;
    }

    private AsReadingTypeDataSelector init(ReadingTypeDataSelectorImpl selector,Logger logger) {
        this.selector = selector;
        this.logger = logger;
        return this;
    }

    static AsReadingTypeDataSelector from(DataModel dataModel, ReadingTypeDataSelectorImpl selector, Logger logger) {
        return dataModel.getInstance(AsReadingTypeDataSelector.class).init(selector, logger);
    }

    @Override
    public Stream<ExportData> selectData(DataExportOccurrence occurrence) {
        Set<IReadingTypeDataExportItem> activeItems;
        Map<IReadingTypeDataExportItem, Optional<Instant>> lastRuns;
        try (TransactionContext context = transactionService.getContext()) {
            activeItems = selector.getActiveItems(occurrence);

            selector.getExportItems().stream()
                    .filter(item -> !activeItems.contains(item))
                    .peek(IReadingTypeDataExportItem::deactivate)
                    .forEach(IReadingTypeDataExportItem::update);
            lastRuns = activeItems.stream()
                    .collect(Collectors.toMap(Function.identity(), IReadingTypeDataExportItem::getLastRun));
            activeItems.stream()
                    .peek(IReadingTypeDataExportItem::activate)
                    .peek(item -> item.setLastRun(occurrence.getTriggerTime()))
                    .forEach(IReadingTypeDataExportItem::update);
            context.commit();
        }

        DefaultItemDataSelector defaultItemDataSelector = DefaultItemDataSelector.from(dataModel, logger);
        return activeItems.stream()
                .flatMap(item -> Stream.of(
                        defaultItemDataSelector.selectData(occurrence, item),
                        lastRuns.get(item).flatMap(since -> defaultItemDataSelector.selectDataForUpdate(occurrence, item, since))))
                .flatMap(Functions.asStream());
    }
}
