/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportStrategy;
import com.elster.jupiter.export.DataSelectorConfig;
import com.elster.jupiter.export.DefaultSelectorOccurrence;
import com.elster.jupiter.export.MeterReadingSelectorConfig;
import com.elster.jupiter.export.MissingDataOption;
import com.elster.jupiter.export.ValidatedDataOption;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.Membership;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.orm.JournalEntry;
import com.elster.jupiter.time.RelativePeriod;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.streams.DecoratedStream.decorate;

class MeterReadingSelectorConfigImpl extends ReadingDataSelectorConfigImpl implements MeterReadingSelectorConfig {

    static final String IMPLEMENTOR_NAME = "MeterReadingSelectorConfig";

    private Long endDeviceGroup;
    private MeteringGroupsService meteringGroupsService;

    @Inject
    MeterReadingSelectorConfigImpl(DataModel dataModel, MeteringGroupsService meteringGroupService) {
        super(dataModel);
        meteringGroupsService = meteringGroupService;
    }


    static MeterReadingSelectorConfigImpl from(DataModel dataModel, IExportTask exportTask, RelativePeriod exportPeriod) {
        MeterReadingSelectorConfigImpl config = dataModel.getInstance(MeterReadingSelectorConfigImpl.class);
        config.init(exportTask, exportPeriod);
        return config;
    }

    @Override
    public DataExportStrategy getStrategy() {
        return new DataExportStrategyImpl(
                isExportUpdate(), isExportContinuousData(), isExportOnlyIfComplete(), getValidatedDataOption(), getUpdatePeriod()
                .orElse(null), getUpdateWindow().orElse(null)
        );
    }

    @Override
    public EndDeviceGroup getEndDeviceGroup() {
          return findEndDeviceGroupById();    //lori - aici nu gaseste groupId-ul
    }

    @Override
    public long getEndDeviceGroupId(){
        return endDeviceGroup;
    }

    @Override
    public History<DataSelectorConfig> getHistory() {
        List<JournalEntry<MeterReadingSelectorConfigImpl>> journal = getDataModel().mapper(MeterReadingSelectorConfigImpl.class)
                .getJournal(getId());
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
                .flatMap(rt -> this.readingTypeDataExportItems(rt, occurrence))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Stream<IReadingTypeDataExportItem> readingTypeDataExportItems(ReadingContainer readingContainer, DataExportOccurrence occurrence) {
        Set<ReadingType> readingTypeSet = occurrence.getRetryTime()
                .isPresent() ? getReadingTypes(occurrence.getRetryTime().get()) : getReadingTypes();
        return readingTypeSet.stream()
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
        public MeterReadingSelectorConfig.Updater setExportOnlyIfComplete(MissingDataOption exportOnlyIfComplete) {
            super.setExportOnlyIfComplete(exportOnlyIfComplete);
            return this;
        }

        @Override
        public MeterReadingSelectorConfig.Updater setEndDeviceGroup(EndDeviceGroup group) {
            endDeviceGroup = group.getId();
            return this;
        }

        @Override
        public MeterReadingSelectorConfig.Updater setUpdatePeriod(RelativePeriod period) {
            super.setUpdatePeriod(period);
            return this;
        }

        @Override
        public MeterReadingSelectorConfig.Updater setUpdateWindow(RelativePeriod window) {
            super.setUpdateWindow(window);
            return this;
        }

        @Override
        public MeterReadingSelectorConfig.Updater setExportUpdate(boolean exportUpdateFlag) {
            super.setExportUpdate(exportUpdateFlag);
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

    private EndDeviceGroup findEndDeviceGroupById() {
        if (this.endDeviceGroup != null) {
            return this.meteringGroupsService.findEndDeviceGroup(this.endDeviceGroup).orElse(null);
        } else {
            return null;
        }
    }

}
