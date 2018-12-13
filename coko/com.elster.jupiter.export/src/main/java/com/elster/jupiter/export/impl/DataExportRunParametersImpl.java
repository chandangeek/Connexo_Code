/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.export.DataExportRunParameters;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import java.time.Instant;

/**
 * Created by H165696 on 10/21/2017.
 */
public class DataExportRunParametersImpl implements DataExportRunParameters {

    private Reference<IExportTask> task = ValueReference.absent();
    private final DataModel dataModel;

    private Instant exportPeriodStart;
    private Instant exportPeriodEnd;
    private Instant updatePeriodStart;
    private Instant updatePeriodEnd;
    private Instant createDateTime;


    @Inject
    DataExportRunParametersImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public IExportTask getTask() {
        return this.task.get();
    }

    @Override
    public Instant getExportPeriodStart() {
        return exportPeriodStart;
    }

    @Override
    public Instant getExportPeriodEnd() {
        return exportPeriodEnd;
    }

    @Override
    public Instant getUpdatePeriodStart() {
        return updatePeriodStart;
    }

    @Override
    public Instant getUpdatePeriodEnd() {
        return updatePeriodEnd;
    }

    @Override
    public Instant getCreateDateTime() {
        return createDateTime;
    }

    public void save() {
        Save.CREATE.save(dataModel, this);
    }

    public void delete() {
        Save.UPDATE.save(dataModel, this);
    }

    void initTask(IExportTask task) {
        this.task.set(task);
    }

    DataExportRunParametersImpl init(IExportTask task, Instant createDateTime, Instant exportPeriodStart, Instant exportPeriodEnd, Instant updatePeriodStart, Instant updatePeriodEnd) {
        initTask(task);
        this.createDateTime = createDateTime;
        this.exportPeriodStart = exportPeriodStart;
        this.exportPeriodEnd = exportPeriodEnd;
        this.updatePeriodStart = updatePeriodStart;
        this.updatePeriodEnd = updatePeriodEnd;
        return this;
    }

}
