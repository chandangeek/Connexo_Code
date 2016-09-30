package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataSelector;
import com.elster.jupiter.export.DefaultSelectorOccurrence;
import com.elster.jupiter.export.ExportData;
import com.elster.jupiter.export.MeterReadingData;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EndDeviceMembership;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.streams.Functions;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.streams.DecoratedStream.decorate;

class ReadingTypeDataSelector implements DataSelector {
    private final DataModel dataModel;
    private final TransactionService transactionService;
    private final Thesaurus thesaurus;

    private Logger logger;
    private StandardDataSelectorImpl selector;

    @Inject
    ReadingTypeDataSelector(DataModel dataModel, TransactionService transactionService, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.transactionService = transactionService;
        this.thesaurus = thesaurus;
    }

    private ReadingTypeDataSelector init(StandardDataSelectorImpl selector, Logger logger) {
        this.selector = selector;
        this.logger = logger;
        return this;
    }

    static ReadingTypeDataSelector from(DataModel dataModel, StandardDataSelectorImpl selector, Logger logger) {
        return dataModel.getInstance(ReadingTypeDataSelector.class).init(selector, logger);
    }

    @Override
    public Stream<ExportData> selectData(DataExportOccurrence occurrence) {
        Set<IReadingTypeDataExportItem> activeItems;
        Map<IReadingTypeDataExportItem, Optional<Instant>> lastRuns;
        try (TransactionContext context = transactionService.getContext()) {
            activeItems = selector.getActiveItems(occurrence);

            getExportItems().stream()
                    .filter(item -> !activeItems.contains(item))
                    .peek(IReadingTypeDataExportItem::deactivate)
                    .forEach(IReadingTypeDataExportItem::update);
            lastRuns = activeItems.stream()
                    .collect(Collectors.toMap(Function.identity(), ReadingTypeDataExportItem::getLastRun));
            activeItems.forEach(IReadingTypeDataExportItem::activate);
            warnIfDevicesHaveNoneOfTheReadingTypes(logger, occurrence);
            context.commit();
        }

        DefaultItemDataSelector defaultItemDataSelector = DefaultItemDataSelector.from(dataModel, logger);
        try {
            Map<IReadingTypeDataExportItem, Optional<MeterReadingData>> selectedData = activeItems.stream()
                    .collect(Collectors.toMap(
                            Function.identity(),
                            activeItem -> defaultItemDataSelector.selectData(occurrence, activeItem)
                    ));
            long numberOfItemsExported = selectedData.values()
                    .stream()
                    .filter(Optional::isPresent)
                    .count();
            long numberOfItemsSkipped = activeItems.size() - numberOfItemsExported;
            ((IDataExportOccurrence) occurrence).summarize(
                    thesaurus.getFormat(TranslationKeys.NUMBER_OF_DATASOURCES_SUCCESSFULLY_EXPORTED).format(numberOfItemsExported) +
                            System.getProperty("line.separator") +
                            thesaurus.getFormat(TranslationKeys.NUMBER_OF_DATASOURCES_SKIPPED).format(numberOfItemsSkipped));

            return activeItems.stream()
                    .flatMap(item -> Stream.of(
                                    selectedData.get(item),
                                    lastRuns.get(item).flatMap(since -> defaultItemDataSelector.selectDataForUpdate(occurrence, item, since)))
                    )
                    .flatMap(Functions.asStream());
        } finally {
            try (TransactionContext context = transactionService.getContext()) {
                if (defaultItemDataSelector.getExportCount() == 0) {
                    MessageSeeds.NO_DATA_TOEXPORT.log(logger, thesaurus);
                    context.commit();
                }
            }
        }
    }

    private void warnIfDevicesHaveNoneOfTheReadingTypes(Logger logger, DataExportOccurrence occurrence) {
        Range<Instant> range = occurrence.getDefaultSelectorOccurrence()
                .map(DefaultSelectorOccurrence::getExportedDataInterval)
                .orElse(Range.all());
        boolean hasMismatchedMeters = decorate(getEndDeviceGroup()
                .getMembers(range)
                .stream())
                .map(EndDeviceMembership::getEndDevice)
                .filterSubType(Meter.class)
                .anyMatch(meter -> meter.getReadingTypes(range)
                                .stream()
                                .noneMatch(readingType -> getReadingTypes().contains(readingType))
                );
        if (hasMismatchedMeters) {
            MessageSeeds.SOME_DEVICES_HAVE_NONE_OF_THE_SELECTED_READINGTYPES.log(logger, thesaurus, getEndDeviceGroup().getName());
        }
    }

    private EndDeviceGroup getEndDeviceGroup() {
        return selector.getEndDeviceGroup();
    }

    private List<IReadingTypeDataExportItem> getExportItems() {
        return selector.getExportItems();
    }

    private Set<ReadingType> getReadingTypes() {
        return selector.getReadingTypes();
    }

    public RelativePeriod getExportPeriod() {
        return selector.getExportPeriod();
    }
}

