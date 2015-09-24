package com.elster.jupiter.export.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.ReadingTypeDataSelector;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.RefAny;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

public class ReadingTypeDataExportItemImpl implements IReadingTypeDataExportItem {

    private final MeteringService meteringService;
    private final DataExportService dataExportService;

    private long id;
    private Instant lastRun;
    private Instant lastExportedDate;
    private String readingTypeMRId;
    private RefAny readingContainer;
    private Reference<IReadingTypeDataSelector> selector = ValueReference.absent();
    private boolean active = true;

    private transient DataModel dataModel;
    private transient ReadingType readingType;


    @Inject
    public ReadingTypeDataExportItemImpl(MeteringService meteringService, IDataExportService dataExportService, DataModel model) {
        this.meteringService = meteringService;
        this.dataExportService = dataExportService;
        dataModel = model;
    }

    static ReadingTypeDataExportItemImpl from(DataModel model, IReadingTypeDataSelector dataSelector, ReadingContainer readingContainer, ReadingType readingType) {
        return model.getInstance(ReadingTypeDataExportItemImpl.class).init(dataSelector, readingContainer, readingType);
    }

    private ReadingTypeDataExportItemImpl init(IReadingTypeDataSelector dataSelector, ReadingContainer readingContainer, ReadingType readingType) {
        this.selector.set(dataSelector);
        this.readingTypeMRId = readingType.getMRID();
        this.readingType = readingType;
        this.readingContainer = dataModel.asRefAny(readingContainer);
        return this;
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
    public ReadingTypeDataSelector getSelector() {
        return selector.orElseThrow(IllegalStateException::new);
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
    public void update() {
        Save.UPDATE.save(dataModel, this);
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
}
