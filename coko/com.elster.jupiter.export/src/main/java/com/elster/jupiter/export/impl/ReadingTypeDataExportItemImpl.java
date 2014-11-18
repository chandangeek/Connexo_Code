package com.elster.jupiter.export.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.export.ReadingTypeDataExportTask;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.RefAny;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 * Date: 5/11/2014
 * Time: 13:28
 */
public class ReadingTypeDataExportItemImpl implements IReadingTypeDataExportItem {

    private final MeteringService meteringService;

    private long id;
    private Instant lastRun;
    private Instant lastExportedDate;
    private String readingTypeMRId;
    private RefAny readingContainer;
    private Reference<IReadingTypeDataExportTask> task = ValueReference.absent();

    private transient DataModel dataModel;
    private transient ReadingType readingType;


    @Inject
    public ReadingTypeDataExportItemImpl(MeteringService meteringService, DataModel model) {
        this.meteringService = meteringService;
        dataModel = model;
    }

    static ReadingTypeDataExportItemImpl from(DataModel model, IReadingTypeDataExportTask readingTypeExportTask, ReadingContainer readingContainer, ReadingType readingType) {
        return model.getInstance(ReadingTypeDataExportItemImpl.class).init(readingTypeExportTask, readingContainer, readingType);
    }

    private ReadingTypeDataExportItemImpl init(IReadingTypeDataExportTask readingTypeDataExportTask, ReadingContainer readingContainer, ReadingType readingType) {
        this.task.set(readingTypeDataExportTask);
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
    public ReadingTypeDataExportTask getTask() {
        return task.orElseThrow(IllegalStateException::new);
    }

    @Override
    public void updateLastRunAndLastExported(Instant lastRun, Instant lastExported) {
        this.lastRun = lastRun;
        this.lastExportedDate = lastExported;
    }

    @Override
    public void update() {
        Save.UPDATE.save(dataModel, this);
    }
}
