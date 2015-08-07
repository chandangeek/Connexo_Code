package com.energyict.mdc.device.data;

import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

@ProviderType
public interface BatchService {

    Batch findOrCreateBatch(String name);

    Optional<Batch> findBatch(Device device);

}
