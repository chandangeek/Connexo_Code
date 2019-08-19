/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataSelector;
import com.elster.jupiter.export.ExportData;
import com.elster.jupiter.export.MeterReadingData;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.streams.Functions;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract class AbstractDataSelector implements DataSelector {

    private final DataModel dataModel;
    private final TransactionService transactionService;
    private final Thesaurus thesaurus;

    private Logger logger;

    AbstractDataSelector(DataModel dataModel, TransactionService transactionService, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.transactionService = transactionService;
        this.thesaurus = thesaurus;
    }

    AbstractDataSelector init(Logger logger) {
        this.logger = logger;
        return this;
    }

    @Override
    public Stream<ExportData> selectData(DataExportOccurrence occurrence) {
        Set<ReadingTypeDataExportItem> activeItems = manageActiveItems(occurrence);
        Map<Long, Optional<Instant>> lastRuns = activeItems.stream().collect(Collectors.toMap(ReadingTypeDataExportItem::getId, ReadingTypeDataExportItem::getLastRun));
        AbstractItemDataSelector itemDataSelector = getItemDataSelector();

        try {
            Map<Long, Optional<MeterReadingData>> selectedData = new LinkedHashMap<>();
            Map<Long, Optional<MeterReadingData>> updateData = new HashMap<>();
            for (ReadingTypeDataExportItem activeItem : activeItems) {
                selectedData.put(activeItem.getId(), itemDataSelector.selectData(occurrence, activeItem));
                if (lastRuns.containsKey(activeItem.getId()) && lastRuns.get(activeItem.getId()).isPresent()) {
                    updateData.put(activeItem.getId(), itemDataSelector.selectDataForUpdate(occurrence, activeItem, lastRuns.get(activeItem.getId()).get()));
                } else {
                    updateData.put(activeItem.getId(), Optional.empty());
                }
                activeItem.clearCachedReadingContainer();
            }

            long numberOfItemsExported = selectedData.values().stream().filter(Optional::isPresent).count();

            long numberOfItemsSkipped = activeItems.size() - numberOfItemsExported;

            ((IDataExportOccurrence) occurrence).summarize(
                    getThesaurus().getFormat(TranslationKeys.NUMBER_OF_DATASOURCES_SUCCESSFULLY_EXPORTED).format(numberOfItemsExported) +
                            System.getProperty("line.separator") +
                            getThesaurus().getFormat(TranslationKeys.NUMBER_OF_DATASOURCES_SKIPPED).format(numberOfItemsSkipped));

            List<ExportData> collect = selectedData.entrySet().stream()
                    .flatMap(item -> Stream.of(
                            item.getValue(),
                            updateData.get(item.getKey())))
                    .flatMap(Functions.asStream()).collect(Collectors.toList());
            return collect.stream();
        } finally {
            if (itemDataSelector.getExportCount() == 0) {
                try (TransactionContext context = getTransactionService().getContext()) {
                    MessageSeeds.NO_DATA_TOEXPORT.log(getLogger(), getThesaurus());
                    context.commit();
                }
            }
        }
    }

    private Set<ReadingTypeDataExportItem> manageActiveItems(DataExportOccurrence occurrence) {
        Set<ReadingTypeDataExportItem> activeItems;
        try (TransactionContext context = getTransactionService().getContext()) {
            activeItems = getSelectorConfig().getActiveItems(occurrence);
            getSelectorConfig().getExportItems().stream()
                    .filter(item -> !activeItems.contains(item))
                    .peek(ReadingTypeDataExportItem::deactivate)
                    .forEach(ReadingTypeDataExportItem::update);
            activeItems.forEach(ReadingTypeDataExportItem::activate);
            warnIfObjectsHaveNoneOfTheReadingTypes(occurrence);
            context.commit();
        }
        return activeItems;
    }

    abstract void warnIfObjectsHaveNoneOfTheReadingTypes(DataExportOccurrence occurrence);

    abstract AbstractItemDataSelector getItemDataSelector();

    abstract ReadingDataSelectorConfigImpl getSelectorConfig();

    DataModel getDataModel() {
        return dataModel;
    }

    Logger getLogger() {
        return logger;
    }

    Thesaurus getThesaurus() {
        return thesaurus;
    }

    TransactionService getTransactionService() {
        return transactionService;
    }
}
