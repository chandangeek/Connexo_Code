/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.device.data.Batch;
import com.energyict.mdc.device.data.Device;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.time.Instant;

public class BatchImpl implements Batch {

    enum Fields {

        BATCH_NAME("batch");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    private long id;
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String batch;

    //audit fields
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    private final DataModel dataModel;

    @Inject
    private BatchImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public static BatchImpl from(DataModel dataModel, String name) {
        return dataModel.getInstance(BatchImpl.class).init(name);
    }

    BatchImpl init(String name) {
        this.batch = name;
        return this;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return batch;
    }

    @Override
    public boolean addDevice(Device device) {
        if (device.getBatch().isPresent() && device.getBatch().get().getId() == getId()) {
            return false;
        }
        device.addInBatch(this);
        return true;
    }

    @Override
    public void removeDevice(Device device) {
        device.removeFromBatch(this);
    }

    @Override
    public boolean isMember(Device device) {
        return device.getBatch().isPresent() && device.getBatch().get().getId() == getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BatchImpl batch = (BatchImpl) o;
        return id == batch.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}