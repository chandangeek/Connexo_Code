package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.data.Batch;
import com.energyict.mdc.device.data.Device;

import javax.inject.Inject;
import java.time.Instant;

public class DeviceInBatch {

    enum Fields {
        DEVICE("device"),
        BATCH("batch"),
        CREATE_TIME("createTime");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    @IsPresent
    private Reference<Device> device = ValueReference.absent();
    @IsPresent
    private Reference<Batch> batch = ValueReference.absent();

    @SuppressWarnings("unused")
    private Instant createTime;

    private final DataModel dataModel;

    @Inject
    private DeviceInBatch(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    DeviceInBatch init(Device device, Batch batch) {
        this.device.set(device);
        this.batch.set(batch);
        return this;
    }

    static DeviceInBatch from(DataModel dataModel, Device device, Batch batch) {
        return dataModel.getInstance(DeviceInBatch.class).init(device, batch);
    }

    public Batch getBatch() {
        return batch.get();
    }

    public Device getDevice() {
        return device.get();
    }

    public void persist() {
        dataModel.mapper(DeviceInBatch.class).persist(this);
    }

    public void remove() {
        dataModel.mapper(DeviceInBatch.class).remove(this);
    }
}
