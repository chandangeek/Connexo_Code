package com.energyict.mdc.device.data;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@ProviderType
public interface BatchService {

    Batch findOrCreateBatch(String name);

    Optional<Batch> findBatch(Device device);

    Map<Device, Batch> findBatches(List<Device> devices);
}
