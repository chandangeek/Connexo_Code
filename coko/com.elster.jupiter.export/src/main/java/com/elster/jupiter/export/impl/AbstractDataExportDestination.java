package com.elster.jupiter.export.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.export.DataExportDestination;
import com.elster.jupiter.export.EmailDestination;
import com.elster.jupiter.export.FileDestination;
import com.elster.jupiter.export.ReadingTypeDataExportTask;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.google.common.collect.ImmutableMap;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;


/**
 * Created by igh on 22/05/2015.
 */
public class AbstractDataExportDestination implements DataExportDestination {

    static final Map<String, Class<? extends DataExportDestination>> IMPLEMENTERS = ImmutableMap.<String, Class<? extends DataExportDestination>>of(FileDestination.TYPE_IDENTIFIER, FileDestinationImpl.class, EmailDestination.TYPE_IDENTIFIER, EmailDestinationImpl.class);

    private long id;
    private Reference<ReadingTypeDataExportTask> task = ValueReference.absent();
    private final DataModel dataModel;

    @Inject
    AbstractDataExportDestination(DataModel dataModel) {
        this.dataModel = dataModel;
    }


    public ReadingTypeDataExportTask getTask() {
        return this.task.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        return id == ((AbstractDataExportDestination) o).id;
    }

    @Override
    public long getId() {
        return id;
    }

    public void save() {
        if (getId() == 0) {
            doPersist();
            return;
        }
        doUpdate();
    }

    private void doPersist() {
        Save.CREATE.save(dataModel, this);
    }

    private void doUpdate() {
        Save.UPDATE.save(dataModel, this);
    }

    public void delete() {
        doUpdate();
    }

}
