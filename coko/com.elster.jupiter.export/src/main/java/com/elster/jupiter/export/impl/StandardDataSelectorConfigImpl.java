/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.export.DataSelectorConfig;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.time.RelativePeriod;

import javax.inject.Inject;
import java.time.Instant;

abstract class StandardDataSelectorConfigImpl implements DataSelectorConfig {

    private final DataModel dataModel;

    private long id;

    @IsPresent(groups = {Save.Create.class, Save.Update.class})
    private Reference<IExportTask> exportTask = ValueReference.absent();

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    private Reference<RelativePeriod> exportPeriod = ValueReference.absent();

    private boolean exportContinuousData;

    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    @Inject
    StandardDataSelectorConfigImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    void init(IExportTask exportTask, RelativePeriod exportPeriod) {
        this.exportTask.set(exportTask);
        this.exportPeriod.set(exportPeriod);
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public RelativePeriod getExportPeriod() {
        return exportPeriod.get();
    }

    @Override
    public boolean isExportContinuousData() {
        return exportContinuousData;
    }

    @Override
    public IExportTask getExportTask() {
        return exportTask.get();
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public Instant getCreateTime() {
        return createTime;
    }

    @Override
    public Instant getModTime() {
        return modTime;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    DataModel getDataModel() {
        return dataModel;
    }

    public void save() {
        Save.action(id).save(getDataModel(), this);
    }

    public void delete() {
        getDataModel().remove(this);
    }

    abstract class UpdaterImpl implements Updater {

        @Override
        public Updater setExportPeriod(RelativePeriod period) {
            exportPeriod.set(period);
            return this;
        }

        @Override
        public Updater setExportContinuousData(boolean exportContinuousDataFlag) {
            exportContinuousData = exportContinuousDataFlag;
            return this;
        }
    }
}
