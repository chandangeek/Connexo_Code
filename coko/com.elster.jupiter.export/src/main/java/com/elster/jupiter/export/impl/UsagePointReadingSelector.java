package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DefaultSelectorOccurrence;
import com.elster.jupiter.metering.groups.Membership;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.streams.Functions;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Instant;
import java.util.logging.Logger;

import static com.elster.jupiter.util.streams.DecoratedStream.decorate;

class UsagePointReadingSelector extends AbstractDataSelector {

    private UsagePointReadingSelectorConfigImpl selectorConfig;

    @Inject
    UsagePointReadingSelector(DataModel dataModel, TransactionService transactionService, Thesaurus thesaurus) {
        super(dataModel, transactionService, thesaurus);
    }

    static UsagePointReadingSelector from(DataModel dataModel, UsagePointReadingSelectorConfigImpl selectorConfig, Logger logger) {
        return dataModel.getInstance(UsagePointReadingSelector.class).init(selectorConfig, logger);
    }

    UsagePointReadingSelector init(UsagePointReadingSelectorConfigImpl selectorConfig, Logger logger) {
        super.init(logger);
        this.selectorConfig = selectorConfig;
        return this;
    }

    @Override
    UsagePointReadingSelectorConfigImpl getSelectorConfig() {
        return selectorConfig;
    }

    @Override
    void warnIfObjectsHaveNoneOfTheReadingTypes(DataExportOccurrence occurrence) {
        warnIfUsagePointsHaveNoneOfTheReadingTypes(occurrence);
    }

    private void warnIfUsagePointsHaveNoneOfTheReadingTypes(DataExportOccurrence occurrence) {
        Range<Instant> range = occurrence.getDefaultSelectorOccurrence()
                .map(DefaultSelectorOccurrence::getExportedDataInterval)
                .orElse(Range.all());
        boolean hasMismatchedUsagePoints = decorate(getUsagePointGroup()
                .getMembers(range)
                .stream())
                .map(Membership::getMember)
                .flatMap(usagePoint -> usagePoint.getEffectiveMetrologyConfigurations(range).stream())
                .flatMap(effectiveMC ->
                        effectiveMC.getMetrologyConfiguration().getContracts().stream()
                                .map(effectiveMC::getChannelsContainer)
                                .flatMap(Functions.asStream())
                )
                .anyMatch(channelsContainer ->
                        channelsContainer.getReadingTypes(range).stream()
                                .noneMatch(readingType -> getSelectorConfig().getReadingTypes().contains(readingType))
                );
        if (hasMismatchedUsagePoints) {
            MessageSeeds.SOME_USAGEPOINTS_HAVE_NONE_OF_THE_SELECTED_READINGTYPES.log(getLogger(), getThesaurus(), getUsagePointGroup().getName());
        }
    }

    @Override
    AbstractItemDataSelector getItemDataSelector() {
        return getDataModel().getInstance(UsagePointReadingItemDataSelector.class).init(getLogger());
    }

    private UsagePointGroup getUsagePointGroup() {
        return getSelectorConfig().getUsagePointGroup();
    }
}
