/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.energyict.mdc.common.device.data.Batch;
import com.energyict.mdc.device.data.BatchService;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

public class BatchServiceImpl implements BatchService {

    private final DataModel dataModel;

    @Inject
    public BatchServiceImpl(DeviceDataModelService deviceDataModelService) {
        this.dataModel = deviceDataModelService.dataModel();
    }

    @Override
    public Batch findOrCreateBatch(String name) {
        Optional<Batch> batchOptional = findBatch(name);
        if (batchOptional.isPresent()) {
            return batchOptional.get();
        } else {
            try {
                return createBatch(name);
            } catch (UnderlyingSQLFailedException e) {
                return findBatch(name).get();
            }
        }
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
}
