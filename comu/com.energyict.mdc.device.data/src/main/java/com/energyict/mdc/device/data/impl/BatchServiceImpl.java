package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Operator;
import com.energyict.mdc.device.data.Batch;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.Device;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class BatchServiceImpl implements BatchService {

    private final DataModel dataModel;

    @Inject
    public BatchServiceImpl(DeviceDataModelService deviceDataModelService) {
        this.dataModel = deviceDataModelService.dataModel();
    }

    @Override
    public Optional<Batch> findBatch(Device device) {
        return dataModel.mapper(DeviceInBatch.class).getOptional(device.getId()).map(DeviceInBatch::getBatch);
    }

    @Override
    public Batch findOrCreateBatch(String name) {
        return findBatch(name).orElseGet(() -> this.createBatch(name));
    }

    private Optional<Batch> findBatch(String name) {
        Condition condition = Operator.EQUALIGNORECASE.compare(BatchImpl.Fields.BATCH_NAME.fieldName(), name);
        List<Batch> batches = dataModel.query(Batch.class).select(condition);
        return batches.stream().findFirst();
    }

    private Batch createBatch(String name) {
        BatchImpl batch = BatchImpl.from(dataModel, name);
        Save.CREATE.save(dataModel, batch);
        return batch;
    }

    @Override
    public Map<Device, Batch> findBatches(List<Device> devices) {
        if (devices.isEmpty()) {
            return Collections.emptyMap();
        }
        QueryExecutor<DeviceInBatch> query = dataModel.query(DeviceInBatch.class, Batch.class);
        List<DeviceInBatch> deviceInBatches = query.select(ListOperator.IN.contains(DeviceInBatch.Fields.DEVICE.fieldName(), devices));
        return deviceInBatches.stream().collect(Collectors.toMap(DeviceInBatch::getDevice, DeviceInBatch::getBatch));
    }


}
