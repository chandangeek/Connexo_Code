package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataSelector;
import com.elster.jupiter.export.DefaultSelectorOccurrence;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.groups.UsagePointMembership;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.transaction.TransactionService;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.DecoratedStream.decorate;

class AggregatedDataSelector extends AbstractDataSelector {

    @Inject
    AggregatedDataSelector(DataModel dataModel, TransactionService transactionService, Thesaurus thesaurus) {
        super(dataModel, transactionService, thesaurus);
    }

    static DataSelector from(DataModel dataModel, StandardDataSelectorImpl selector, Logger logger) {
        return dataModel.getInstance(AggregatedDataSelector.class).init(selector, logger);
    }

    @Override
    void warnIfObjectsHaveNoneOfTheReadingTypes(DataExportOccurrence occurrence) {
        // TODO
    }

    @Override
    AbstractItemDataSelector getItemDataSelector() {
        return getDataModel().getInstance(AggregatedDataItemDataSelector.class).init(getLogger());
    }

    @Override
    Set<IReadingTypeDataExportItem> getActiveItems(DataExportOccurrence occurrence) {
        Range<Instant> exportInterval = occurrence.getDefaultSelectorOccurrence()
                .map(DefaultSelectorOccurrence::getExportedDataInterval)
                .orElse(Range.all());
        return decorate(getSelector().getUsagePointGroup().getMembers(exportInterval).stream())
                .map(UsagePointMembership::getUsagePoint)
                .filterSubType(UsagePoint.class)
                .flatMap(usagePoint -> usagePoint.getEffectiveMetrologyConfigurations(exportInterval).stream())
                .flatMap(effectiveMetrologyConfiguration ->
                        effectiveMetrologyConfiguration.getMetrologyConfiguration().getContracts().stream()
                                .map(effectiveMetrologyConfiguration::getChannelsContainer)
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                )
                .flatMap(this::readingTypeDataExportItems)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
