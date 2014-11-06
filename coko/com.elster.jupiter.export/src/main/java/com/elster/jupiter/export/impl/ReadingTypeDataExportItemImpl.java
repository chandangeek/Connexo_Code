package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.export.ReadingTypeDataExportTask;
import com.elster.jupiter.metering.ReadingContainer;
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
public class ReadingTypeDataExportItemImpl implements ReadingTypeDataExportItem {

    private long id;
    private Instant lastRun;
    private Instant lastExportedDate;
    private String readingTypeMRId;
    private RefAny readingContainer;
    private Reference<IReadingTypeDataExportTask> task = ValueReference.absent();

    private transient DataModel dataModel;


    @Inject
    public ReadingTypeDataExportItemImpl(DataModel model) {
        dataModel = model;
    }

    static ReadingTypeDataExportItemImpl from(DataModel model, IReadingTypeDataExportTask readingTypeExportTask, ReadingContainer readingContainer, String readingTypeMRId) {
        return model.getInstance(ReadingTypeDataExportItemImpl.class).init(readingTypeExportTask, readingContainer, readingTypeMRId);
    }

    private ReadingTypeDataExportItemImpl init(IReadingTypeDataExportTask readingTypeDataExportTask, ReadingContainer readingContainer, String readingTypeMRId) {
        this.task.set(readingTypeDataExportTask);
        this.readingTypeMRId = readingTypeMRId;
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
    public String getReadingTypeMRId() {
        return readingTypeMRId;
    }

    @Override
    public Optional<Instant> getLastExportedDate() {
        return Optional.ofNullable(lastExportedDate);
    }

    @Override
    public ReadingTypeDataExportTask getTask() {
        return task.orElseThrow(IllegalStateException::new);
    }
}
