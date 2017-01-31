/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportStrategy;
import com.elster.jupiter.export.DataSelectorConfig;
import com.elster.jupiter.export.DefaultSelectorOccurrence;
import com.elster.jupiter.export.MeterReadingSelectorConfig;
import com.elster.jupiter.export.ValidatedDataOption;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.Membership;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.orm.JournalEntry;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.time.RelativePeriod;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.streams.DecoratedStream.decorate;

class MeterReadingSelectorConfigImpl extends ReadingDataSelectorConfigImpl implements MeterReadingSelectorConfig {

    static final String IMPLEMENTOR_NAME = "MeterReadingSelectorConfig";

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    private Reference<EndDeviceGroup> endDeviceGroup = ValueReference.absent();

    private boolean exportUpdate;
    private Reference<RelativePeriod> updatePeriod = ValueReference.absent();
    private Reference<RelativePeriod> updateWindow = ValueReference.absent();

    @Inject
    MeterReadingSelectorConfigImpl(DataModel dataModel) {
        super(dataModel);
    }

    static MeterReadingSelectorConfigImpl from(DataModel dataModel, IExportTask exportTask, RelativePeriod exportPeriod) {
        MeterReadingSelectorConfigImpl config = dataModel.getInstance(MeterReadingSelectorConfigImpl.class);
        config.init(exportTask, exportPeriod);
        return config;
    }

    @Override
    public DataExportStrategy getStrategy() {
        return new DataExportStrategyImpl(
                isExportUpdate(), isExportContinuousData(), isExportOnlyIfComplete(), getValidatedDataOption(), getUpdatePeriod().orElse(null), getUpdateWindow().orElse(null)
        );
    }

    @Override
    public EndDeviceGroup getEndDeviceGroup() {
        return endDeviceGroup.get();
    }

    @Override
    public boolean isExportUpdate() {
        return exportUpdate;
    }

    @Override
    public Optional<RelativePeriod> getUpdatePeriod() {
        return updatePeriod.getOptional();
    }

    @Override
    public Optional<RelativePeriod> getUpdateWindow() {
        return updateWindow.getOptional();
    }

    @Override
    public History<DataSelectorConfig> getHistory() {
        List<JournalEntry<MeterReadingSelectorConfigImpl>> journal = getDataModel().mapper(MeterReadingSelectorConfigImpl.class).getJournal(getId());
        return new History<>(journal, this);
    }

    @Override
    public MeterReadingSelectorConfig.Updater startUpdate() {
        return new UpdaterImpl();
    }

    @Override
    Set<IReadingTypeDataExportItem> getActiveItems(DataExportOccurrence occurrence) {
        return decorate(getEndDeviceGroup()
                .getMembers(occurrence.getDefaultSelectorOccurrence()
                        .map(DefaultSelectorOccurrence::getExportedDataInterval)
                        .orElse(Range.all()))
                .stream())
                .map(Membership::getMember)
                .filterSubType(Meter.class)
                .flatMap(this::readingTypeDataExportItems)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Stream<IReadingTypeDataExportItem> readingTypeDataExportItems(ReadingContainer readingContainer) {
        return getReadingTypes().stream()
                .map(r -> getExportItems().stream()
                        .map(IReadingTypeDataExportItem.class::cast)
                        .filter(item -> r.equals(item.getReadingType()))
                        .filter(i -> i.getReadingContainer().is(readingContainer))
                        .findAny()
                        .orElseGet(() -> addExportItem(readingContainer, r))
                );
    }

    @Override
    public MeterReadingSelector createDataSelector(Logger logger) {
        return MeterReadingSelector.from(getDataModel(), this, logger);
    }

    @Override
    public void apply(DataSelectorConfigVisitor visitor) {
        visitor.visit(this);
    }

    class UpdaterImpl extends ReadingDataSelectorConfigImpl.UpdaterImpl implements MeterReadingSelectorConfig.Updater {

        @Override
        public MeterReadingSelectorConfig.Updater addReadingType(ReadingType readingType) {
            super.addReadingType(readingType);
            return this;
        }

        @Override
        public MeterReadingSelectorConfig.Updater removeReadingType(ReadingType readingType) {
            super.removeReadingType(readingType);
            return this;
        }

        @Override
        public MeterReadingSelectorConfig.Updater setValidatedDataOption(ValidatedDataOption option) {
            super.setValidatedDataOption(option);
            return this;
        }

        @Override
        public MeterReadingSelectorConfig.Updater setExportContinuousData(boolean exportContinuousData) {
            super.setExportContinuousData(exportContinuousData);
            return this;
        }

        @Override
        public MeterReadingSelectorConfig.Updater setExportOnlyIfComplete(boolean exportOnlyIfComplete) {
            super.setExportOnlyIfComplete(exportOnlyIfComplete);
            return this;
        }

        @Override
        public MeterReadingSelectorConfig.Updater setEndDeviceGroup(EndDeviceGroup group) {
            endDeviceGroup.set(group);
            return this;
        }

        @Override
        public MeterReadingSelectorConfig.Updater setUpdatePeriod(RelativePeriod period) {
            updatePeriod.set(period);
            return this;
        }

        @Override
        public MeterReadingSelectorConfig.Updater setUpdateWindow(RelativePeriod window) {
            updateWindow.set(window);
            return this;
        }

        @Override
        public MeterReadingSelectorConfig.Updater setExportUpdate(boolean exportUpdateFlag) {
            exportUpdate = exportUpdateFlag;
            return this;
        }

        @Override
        public MeterReadingSelectorConfig.Updater setExportPeriod(RelativePeriod period) {
            super.setExportPeriod(period);
            return this;
        }

        @Override
        public MeterReadingSelectorConfig complete() {
            return MeterReadingSelectorConfigImpl.this;
        }
    }
}
