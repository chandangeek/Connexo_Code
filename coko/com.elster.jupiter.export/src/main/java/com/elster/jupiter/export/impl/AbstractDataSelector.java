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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
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
        Set<IReadingTypeDataExportItem> activeItems;
        Map<IReadingTypeDataExportItem, Optional<Instant>> lastRuns;
        try (TransactionContext context = getTransactionService().getContext()) {
            activeItems = getSelectorConfig().getActiveItems(occurrence);
            getSelectorConfig().getExportItems().stream()
                    .filter(item -> !activeItems.contains(item))
                    .peek(IReadingTypeDataExportItem::deactivate)
                    .forEach(IReadingTypeDataExportItem::update);
            lastRuns = activeItems.stream()
                    .collect(Collectors.toMap(Function.identity(), ReadingTypeDataExportItem::getLastRun));
            activeItems.forEach(IReadingTypeDataExportItem::activate);
            warnIfObjectsHaveNoneOfTheReadingTypes(occurrence);
            context.commit();
        }

        AbstractItemDataSelector itemDataSelector = getItemDataSelector();

        try {
            Map<IReadingTypeDataExportItem, Optional<MeterReadingData>> selectedData = activeItems.stream()
                    .collect(Collectors.toMap(
                            Function.identity(),
                            activeItem -> itemDataSelector.selectData(occurrence, activeItem)
                    ));

            long numberOfItemsExported = selectedData.values().stream().filter(Optional::isPresent).count();

            long numberOfItemsSkipped = activeItems.size() - numberOfItemsExported;

            ((IDataExportOccurrence) occurrence).summarize(
                    getThesaurus().getFormat(TranslationKeys.NUMBER_OF_DATASOURCES_SUCCESSFULLY_EXPORTED).format(numberOfItemsExported) +
                            System.getProperty("line.separator") +
                            getThesaurus().getFormat(TranslationKeys.NUMBER_OF_DATASOURCES_SKIPPED).format(numberOfItemsSkipped));

            return activeItems.stream()
                    .flatMap(item -> Stream.of(
                            selectedData.get(item),
                            lastRuns.get(item).flatMap(since -> itemDataSelector.selectDataForUpdate(occurrence, item, since)))
                    ).flatMap(Functions.asStream());
        } finally {
            try (TransactionContext context = getTransactionService().getContext()) {
                if (itemDataSelector.getExportCount() == 0) {
                    MessageSeeds.NO_DATA_TOEXPORT.log(getLogger(), getThesaurus());
                    context.commit();
                }
            }
        }
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
