package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.Batch;
import com.energyict.mdc.device.data.Device;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import org.hibernate.validator.constraints.NotBlank;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.Optional;

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
    @NotBlank(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
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
        Optional<DeviceInBatch> deviceInBatch = dataModel.mapper(DeviceInBatch.class).getOptional(device.getId());
        if (deviceInBatch.isPresent()) {
            if (deviceInBatch.get().getBatch().getId() == this.getId()) {
                return false;
            }
            deviceInBatch.get().remove();
        }
        DeviceInBatch.from(dataModel, device, this).persist();
        return true;
    }

    @Override
    public void removeDevice(Device device) {
        findDeviceInBatch(device).ifPresent(DeviceInBatch::remove);
    }

    @Override
    public boolean isMember(Device device) {
        return findDeviceInBatch(device).isPresent();
    }

    private Optional<DeviceInBatch> findDeviceInBatch(Device device) {
        return dataModel.mapper(DeviceInBatch.class).getUnique(DeviceInBatch.Fields.DEVICE.fieldName(), device, DeviceInBatch.Fields.BATCH.fieldName(), this);
    }

    @Override
    public void delete() {
        dataModel.mapper(Batch.class).remove(this);
    }
}