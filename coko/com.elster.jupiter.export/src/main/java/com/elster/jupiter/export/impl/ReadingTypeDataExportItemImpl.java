package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.RefAny;

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

    private transient DataModel dataModel;


    @Inject
    public ReadingTypeDataExportItemImpl(DataModel model) {
        dataModel = model;
    }

    static ReadingTypeDataExportItemImpl from(DataModel model, Instant lastRun, Instant lastExportedDate, String readingTypeMRId, ReadingContainer readingContainer) {
        return model.getInstance(ReadingTypeDataExportItemImpl.class).init(lastRun, lastExportedDate, readingTypeMRId, readingContainer);
    }

    ReadingTypeDataExportItemImpl init(Instant lastRun, Instant lastExportedDate, String readingTypeMRId, ReadingContainer readingContainer) {
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
}
