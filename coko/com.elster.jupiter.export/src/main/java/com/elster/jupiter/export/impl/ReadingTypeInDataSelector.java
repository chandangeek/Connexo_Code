/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.export.ReadingDataSelectorConfig;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

class ReadingTypeInDataSelector {

    private final MeteringService meteringService;

    @ValidReadingType(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NO_SUCH_READINGTYPE + "}")
    private String readingTypeMRID;

    private transient ReadingType readingType;
    private Reference<ReadingDataSelectorConfig> readingTypeDataSelector = ValueReference.absent();

    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    @Inject
    ReadingTypeInDataSelector(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    ReadingTypeInDataSelector init(ReadingDataSelectorConfig readingDataSelectorConfig, ReadingType readingType) {
        this.readingTypeDataSelector.set(readingDataSelectorConfig);
        this.readingType = readingType;
        this.readingTypeMRID = readingType.getMRID();
        return this;
    }

    static ReadingTypeInDataSelector from(DataModel dataModel, ReadingDataSelectorConfig readingDataSelectorConfig, ReadingType readingType) {
        return dataModel.getInstance(ReadingTypeInDataSelector.class).init(readingDataSelectorConfig, readingType);
    }

    static ReadingTypeInDataSelector from(DataModel dataModel, ReadingDataSelectorConfig readingDataSelectorConfig, String readingTypeMRID) {
        ReadingTypeInDataSelector readingTypeInDataSelector = dataModel.getInstance(ReadingTypeInDataSelector.class);
        readingTypeInDataSelector.readingTypeDataSelector.set(readingDataSelectorConfig);
        readingTypeInDataSelector.readingTypeMRID = readingTypeMRID;
        return readingTypeInDataSelector;
    }

    public ReadingDataSelectorConfig getReadingTypeDataSelector() {
        return readingTypeDataSelector.get();
    }

    public ReadingType getReadingType() {
        if (readingType == null) {
            Optional<ReadingType> optional = meteringService.getReadingType(readingTypeMRID);
            return (optional.isPresent() ? optional.get() : null);
        }
        return readingType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ReadingTypeInDataSelector that = (ReadingTypeInDataSelector) o;

        return readingTypeDataSelector.get().getId() == that.readingTypeDataSelector.get().getId() && readingTypeMRID.equals(that.readingTypeMRID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(readingTypeDataSelector.get().getId(), readingTypeMRID);
    }
}
