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
    private Reference<ReadingTypeDataExportTask> task = ValueReference.absent();

    private transient DataModel dataModel;


    @Inject
    public ReadingTypeDataExportItemImpl(DataModel model) {
        dataModel = model;
    }

    static ReadingTypeDataExportItemImpl from(DataModel model, ReadingTypeDataExportTask readingTypeExportTask, Instant lastRun, Instant lastExportedDate, String readingTypeMRId, ReadingContainer readingContainer) {
        return model.getInstance(ReadingTypeDataExportItemImpl.class).init(readingTypeExportTask, lastRun, lastExportedDate, readingTypeMRId, readingContainer);
    }

    ReadingTypeDataExportItemImpl init(ReadingTypeDataExportTask readingTypeDataExportTask, Instant lastRun, Instant lastExportedDate, String readingTypeMRId, ReadingContainer readingContainer) {
        this.task.set(readingTypeDataExportTask);
        this.lastRun = lastRun;
        this.lastExportedDate = lastExportedDate;
        this.readingTypeMRId = readingTypeMRId;
        this.readingContainer = dataModel.asRefAny(readingContainer);
        return this;
    }

    @Override
    public Instant getLastRun() {
        return lastRun;
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
    public Instant getLastExportedDate() {
        return lastExportedDate;
    }

    @Override
    public ReadingTypeDataExportTask getTask() {
        return task.orElseThrow(IllegalStateException::new);
    }
}
