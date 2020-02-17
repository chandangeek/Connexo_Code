/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.ReadingDataSelectorConfig;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MetrologyContractChannelsContainer;
import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.RefAny;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.time.TimeDuration;

import com.google.common.collect.Range;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;

public class ReadingTypeDataExportItemImpl implements ReadingTypeDataExportItem {

    private final MeteringService meteringService;
    private final DataExportService dataExportService;

    private long id;
    private Instant lastRun;
    private Instant lastExportedDate;
    private Instant lastExportedPeriodEnd;
    private String readingTypeMRId;
    private RefAny readingContainer;
    private Reference<ReadingDataSelectorConfig> selector = ValueReference.absent();
    private boolean active = true;
    private boolean exportPostponedForNewData;
    private boolean exportPostponedForChangedData;
    @Size(min = 3, // 1 symbol per count + space + 1 symbol per unit
            max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_MIN_AND_MAX + "}")
    private String readingInterval;

    private transient DataModel dataModel;
    private transient ReadingType readingType;

    @Inject
    public ReadingTypeDataExportItemImpl(MeteringService meteringService, IDataExportService dataExportService, DataModel model) {
        this.meteringService = meteringService;
        this.dataExportService = dataExportService;
        dataModel = model;
    }

    static ReadingTypeDataExportItemImpl from(DataModel model, ReadingDataSelectorConfig dataSelector, ReadingContainer readingContainer, ReadingType readingType) {
        return model.getInstance(ReadingTypeDataExportItemImpl.class).init(dataSelector, readingContainer, readingType);
    }

    private ReadingTypeDataExportItemImpl init(ReadingDataSelectorConfig dataSelector, ReadingContainer readingContainer, ReadingType readingType) {
        this.selector.set(dataSelector);
        this.readingTypeMRId = readingType.getMRID();
        this.readingType = readingType;
        this.readingContainer = dataModel.asRefAny(readingContainer);
        return this;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Optional<Instant> getLastRun() {
        return Optional.ofNullable(lastRun);
    }

    @Override
    public ReadingContainer getReadingContainer() {
        return (ReadingContainer) readingContainer.get();
    }

    @Override
    public ReadingType getReadingType() {
        if (readingType == null) {
            readingType = meteringService.getReadingType(readingTypeMRId).orElseThrow(IllegalArgumentException::new);
        }
        return readingType;
    }

    @Override
    public Optional<Instant> getLastExportedDate() {
        return Optional.ofNullable(lastExportedDate);
    }

    @Override
    public Optional<Instant> getLastExportedPeriodEnd() {
        return Optional.ofNullable(lastExportedPeriodEnd);
    }

    @Override
    public ReadingDataSelectorConfig getSelector() {
        return selector.orElseThrow(IllegalStateException::new);
    }


    @Override
    public Optional<TimeDuration> getRequestedReadingInterval() {
        return Optional.ofNullable(readingInterval)
                .map(TimeDuration::new);
    }

    @Override
    public boolean isExportPostponedForNewData() {
        return exportPostponedForNewData;
    }

    @Override
    public boolean isExportPostponedForChangedData() {
        return exportPostponedForChangedData;
    }

    @Override
    public void setLastRun(Instant lastRun) {
        this.lastRun = lastRun;
    }

    @Override
    public void setLastExportedDate(Instant lastExportedDate) {
        this.lastExportedDate = lastExportedDate;
    }

    @Override
    public void setLastExportedPeriodEnd(Instant lastExportedPeriodEnd) {
        this.lastExportedPeriodEnd = lastExportedPeriodEnd;
    }

    @Override
    public void postponeExportForNewData() {
        this.exportPostponedForNewData = true;
    }

    @Override
    public void postponeExportForChangedData() {
        this.exportPostponedForChangedData = true;
    }

    @Override
    public void overrideReadingInterval(TimeDuration readingInterval) {
        this.readingInterval = readingInterval.toString();
    }

    @Override
    public void update() {
        dataModel.update(this);
    }

    @Override
    public void activate() {
        active = true;
    }

    @Override
    public void deactivate() {
        active = false;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public Optional<? extends DataExportOccurrence> getLastOccurrence() {
        return getLastRun().flatMap(trigger -> dataExportService.findDataExportOccurrence(getTask(), trigger));
    }

    @Override
    public Optional<Range<Instant>> getLastExportPeriod() {
        return getLastRun()
                .map(instant -> getSelector().getExportPeriod().getOpenClosedInterval(ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())));
    }

    private ExportTask getTask() {
        return getSelector().getExportTask();
    }

    @Override
    public String getDescription() {
        ReadingContainer readingContainer = getReadingContainer();
        String description;
        if (readingContainer instanceof Meter) {
            description = ((Meter) readingContainer).getName();
        } else if (readingContainer instanceof MetrologyContractChannelsContainer) {
            MetrologyContractChannelsContainer metrologyContractChannelsContainer = (MetrologyContractChannelsContainer) readingContainer;
            description = metrologyContractChannelsContainer.getUsagePoint().get().getName()
                    + ':' + metrologyContractChannelsContainer.getMetrologyContract().getMetrologyPurpose().getName();
        } else {
            throw new IllegalStateException("Unexpected domain object linked to export item");
        }
        return description + ":" + getReadingType().getFullAliasName();
    }

    @Override
    public IdentifiedObject getDomainObject() {
        ReadingContainer readingContainer = getReadingContainer();
        if (readingContainer instanceof Meter) {
            return (Meter) readingContainer;
        } else if (readingContainer instanceof ChannelsContainer) {
            return ((ChannelsContainer) readingContainer).getUsagePoint().get();
        } else {
            throw new IllegalStateException("Unexpected domain object linked to export item");
        }
    }

    @Override
    public void clearCachedReadingContainer() {
        readingContainer.clearCachedObject();
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this
                || obj instanceof ReadingTypeDataExportItemImpl
                && ((ReadingTypeDataExportItemImpl) obj).id == id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
