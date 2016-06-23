package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.Batch;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.Device;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by bbl on 18/06/2016.
 */
public class BatchRetriever {

    private final BatchService batchService;
    private Map<Device, Batch> cache;

    public BatchRetriever(BatchService batchService) {
        this(batchService, null);
    }

    public BatchRetriever(BatchService batchService, List<Device> domainObjects) {
        this.batchService = batchService;
        if (domainObjects != null) {
            cache = batchService.findBatches(domainObjects);
        }
    }

    public Optional<Batch> findBatch(Device device) {
        if (cache != null) {
            return cache.containsKey(device) ? Optional.of(cache.get(device)) : Optional.empty();
        } else {
            return batchService.findBatch(device);
        }
    }


}
