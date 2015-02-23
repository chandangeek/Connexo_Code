package com.elster.jupiter.export.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.export.ReadingTypeDataExportTask;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

class ReadingTypeInExportTask {

    private final MeteringService meteringService;

    @ValidReadingType(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NO_SUCH_READINGTYPE + "}")
    private String readingTypeMRID;

    private transient ReadingType readingType;
    private Reference<IReadingTypeDataExportTask> readingTypeDataExportTask = ValueReference.absent();

    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;

    @Inject
    ReadingTypeInExportTask(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    ReadingTypeInExportTask init(IReadingTypeDataExportTask task, ReadingType readingType) {
        this.readingTypeDataExportTask.set(task);
        this.readingType = readingType;
        this.readingTypeMRID = readingType.getMRID();
        return this;
    }

    static ReadingTypeInExportTask from(DataModel dataModel, IReadingTypeDataExportTask task, ReadingType readingType) {
        return dataModel.getInstance(ReadingTypeInExportTask.class).init(task, readingType);
    }

    static ReadingTypeInExportTask from(DataModel dataModel, IReadingTypeDataExportTask task, String readingTypeMRID) {
        ReadingTypeInExportTask readingTypeInExportTask = dataModel.getInstance(ReadingTypeInExportTask.class);
        readingTypeInExportTask.readingTypeDataExportTask.set(task);
        readingTypeInExportTask.readingTypeMRID = readingTypeMRID;
        return readingTypeInExportTask;
    }

    public ReadingTypeDataExportTask getReadingTypeDataExportTask() {
        return readingTypeDataExportTask.get();
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

        ReadingTypeInExportTask that = (ReadingTypeInExportTask) o;

        return readingTypeDataExportTask.get().getId() == that.readingTypeDataExportTask.get().getId() && readingTypeMRID.equals(that.readingTypeMRID);

    }

    @Override
    public int hashCode() {
        return Objects.hash(readingTypeDataExportTask.get().getId(), readingTypeMRID);
    }
}
