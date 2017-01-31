/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataSelectorConfig;
import com.elster.jupiter.export.ReadingDataSelectorConfig;
import com.elster.jupiter.export.ValidatedDataOption;
import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.JournalEntry;

import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

abstract class ReadingDataSelectorConfigImpl extends StandardDataSelectorConfigImpl implements ReadingDataSelectorConfig {

    private boolean exportOnlyIfComplete;
    private ValidatedDataOption validatedDataOption;

    @Valid
    @Size(min = 1, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.MUST_SELECT_AT_LEAST_ONE_READING_TYPE + "}")
    private List<ReadingTypeInDataSelector> readingTypes = new ArrayList<>();

    private List<ReadingTypeDataExportItemImpl> exportItems = new ArrayList<>();

    @Inject
    public ReadingDataSelectorConfigImpl(DataModel dataModel) {
        super(dataModel);
    }

    IReadingTypeDataExportItem addExportItem(ReadingContainer readingContainer, ReadingType readingType) {
        ReadingTypeDataExportItemImpl item = ReadingTypeDataExportItemImpl.from(getDataModel(), ReadingDataSelectorConfigImpl.this, readingContainer, readingType);
        exportItems.add(item);
        return item;
    }

    @Override
    public List<? extends IReadingTypeDataExportItem> getExportItems() {
        return Collections.unmodifiableList(exportItems);
    }

    @Override
    public Set<ReadingType> getReadingTypes() {
        return readingTypes.stream()
                .map(ReadingTypeInDataSelector::getReadingType)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<ReadingType> getReadingTypes(Instant at) {
        List<JournalEntry<ReadingTypeInDataSelector>> readingTypes = getDataModel().mapper(ReadingTypeInDataSelector.class)
                .at(at)
                .find(ImmutableMap.of("readingTypeDataSelector", this));
        return readingTypes.stream()
                .map(JournalEntry::get)
                .map(ReadingTypeInDataSelector::getReadingType)
                .collect(Collectors.toSet());
    }

    @Override
    public ValidatedDataOption getValidatedDataOption() {
        return validatedDataOption;
    }

    @Override
    public boolean isExportOnlyIfComplete() {
        return exportOnlyIfComplete;
    }

    abstract Set<IReadingTypeDataExportItem> getActiveItems(DataExportOccurrence occurrence);

    @Override
    public void delete() {
        readingTypes.clear();
        exportItems.clear();
        super.delete();
    }

    abstract class UpdaterImpl extends StandardDataSelectorConfigImpl.UpdaterImpl implements ReadingDataSelectorConfig.Updater {

        @Override
        public ReadingDataSelectorConfig.Updater addReadingType(ReadingType readingType) {
            if (getReadingTypes().contains(readingType)) {
                return this;
            }
            readingTypes.add(ReadingTypeInDataSelector.from(getDataModel(), ReadingDataSelectorConfigImpl.this, readingType));
            return this;
        }

        @Override
        public ReadingDataSelectorConfig.Updater removeReadingType(ReadingType readingType) {
            readingTypes.removeIf(r -> r.getReadingType().equals(readingType));
            return this;
        }

        @Override
        public ReadingDataSelectorConfig.Updater setValidatedDataOption(ValidatedDataOption option) {
            validatedDataOption = option;
            return this;
        }

        @Override
        public ReadingDataSelectorConfig.Updater setExportContinuousData(boolean exportContinuousDataFlag) {
            super.setExportContinuousData(exportContinuousDataFlag);
            return this;
        }

        @Override
        public ReadingDataSelectorConfig.Updater setExportOnlyIfComplete(boolean exportOnlyIfCompleteFlag) {
            exportOnlyIfComplete = exportOnlyIfCompleteFlag;
            return this;
        }

        @Override
        public DataSelectorConfig complete() {
            return ReadingDataSelectorConfigImpl.this;
        }
    }
}
