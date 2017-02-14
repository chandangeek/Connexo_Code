/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportStrategy;
import com.elster.jupiter.export.DataSelectorConfig;
import com.elster.jupiter.export.DefaultSelectorOccurrence;
import com.elster.jupiter.export.UsagePointReadingSelectorConfig;
import com.elster.jupiter.export.ValidatedDataOption;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.Membership;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.orm.JournalEntry;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.util.streams.Functions;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.streams.DecoratedStream.decorate;

class UsagePointReadingSelectorConfigImpl extends ReadingDataSelectorConfigImpl implements UsagePointReadingSelectorConfig {

    static final String IMPLEMENTOR_NAME = "UsagePointReadingSelectorConfig";

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    private Reference<UsagePointGroup> usagePointGroup = ValueReference.absent();

    @Inject
    public UsagePointReadingSelectorConfigImpl(DataModel dataModel) {
        super(dataModel);
    }

    static UsagePointReadingSelectorConfigImpl from(DataModel dataModel, IExportTask exportTask, RelativePeriod exportPeriod) {
        UsagePointReadingSelectorConfigImpl config = dataModel.getInstance(UsagePointReadingSelectorConfigImpl.class);
        config.init(exportTask, exportPeriod);
        return config;
    }

    @Override
    public UsagePointGroup getUsagePointGroup() {
        return usagePointGroup.get();
    }

    @Override
    public DataExportStrategy getStrategy() {
        return new DataExportStrategyImpl(false, isExportContinuousData(), isExportOnlyIfComplete(), getValidatedDataOption(), null, null);
    }

    @Override
    public History<DataSelectorConfig> getHistory() {
        List<JournalEntry<UsagePointReadingSelectorConfigImpl>> journal = getDataModel().mapper(UsagePointReadingSelectorConfigImpl.class).getJournal(getId());
        return new History<>(journal, this);
    }

    @Override
    public UsagePointReadingSelectorConfig.Updater startUpdate() {
        return new UpdaterImpl();
    }

    @Override
    Set<IReadingTypeDataExportItem> getActiveItems(DataExportOccurrence occurrence) {
        Range<Instant> exportInterval = occurrence.getDefaultSelectorOccurrence()
                .map(DefaultSelectorOccurrence::getExportedDataInterval)
                .orElse(Range.all());
        return decorate(getUsagePointGroup().getMembers(exportInterval).stream())
                .map(Membership::getMember)
                .flatMap(usagePoint -> usagePoint.getEffectiveMetrologyConfigurations(exportInterval).stream())
                .flatMap(effectiveMetrologyConfiguration ->
                        effectiveMetrologyConfiguration.getMetrologyConfiguration().getContracts().stream()
                                .map(effectiveMetrologyConfiguration::getChannelsContainer)
                                .flatMap(Functions.asStream())
                )
                .flatMap(channelsContainer -> readingTypeDataExportItems(channelsContainer, exportInterval))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Stream<IReadingTypeDataExportItem> readingTypeDataExportItems(ChannelsContainer channelsContainer, Range<Instant> exportInterval) {
        return getFilteredReadingTypes(channelsContainer, exportInterval)
                .map(r -> getExportItems().stream()
                        .map(IReadingTypeDataExportItem.class::cast)
                        .filter(item -> r.equals(item.getReadingType()))
                        .filter(i -> i.getDomainObject().equals(channelsContainer.getUsagePoint().get()))
                        .findAny()
                        .orElseGet(() -> addExportItem(channelsContainer, r))
                );
    }

    private Stream<ReadingType> getFilteredReadingTypes(ChannelsContainer channelsContainer, Range<Instant> exportInterval) {
        Set<ReadingType> providingReadingTypes = channelsContainer.getReadingTypes(exportInterval);
        return getReadingTypes().stream().filter(providingReadingTypes::contains);
    }

    @Override
    public UsagePointReadingSelector createDataSelector(Logger logger) {
        return UsagePointReadingSelector.from(getDataModel(), this, logger);
    }

    @Override
    public void apply(DataSelectorConfigVisitor visitor) {
        visitor.visit(this);
    }

    class UpdaterImpl extends ReadingDataSelectorConfigImpl.UpdaterImpl implements UsagePointReadingSelectorConfig.Updater {

        @Override
        public UsagePointReadingSelectorConfig.Updater setExportPeriod(RelativePeriod period) {
            super.setExportPeriod(period);
            return this;
        }

        @Override
        public UsagePointReadingSelectorConfig.Updater setUsagePointGroup(UsagePointGroup group) {
            usagePointGroup.set(group);
            return this;
        }

        @Override
        public UsagePointReadingSelectorConfig.Updater setExportOnlyIfComplete(boolean exportOnlyIfCompleteFlag) {
            super.setExportOnlyIfComplete(exportOnlyIfCompleteFlag);
            return this;
        }

        @Override
        public UsagePointReadingSelectorConfig.Updater setExportContinuousData(boolean exportContinuousDataFlag) {
            super.setExportContinuousData(exportContinuousDataFlag);
            return this;
        }

        @Override
        public UsagePointReadingSelectorConfig.Updater setValidatedDataOption(ValidatedDataOption option) {
            super.setValidatedDataOption(option);
            return this;
        }

        @Override
        public UsagePointReadingSelectorConfig.Updater removeReadingType(ReadingType readingType) {
            super.removeReadingType(readingType);
            return this;
        }

        @Override
        public UsagePointReadingSelectorConfig.Updater addReadingType(ReadingType readingType) {
            super.addReadingType(readingType);
            return this;
        }

        @Override
        public UsagePointReadingSelectorConfig complete() {
            return UsagePointReadingSelectorConfigImpl.this;
        }
    }
}
