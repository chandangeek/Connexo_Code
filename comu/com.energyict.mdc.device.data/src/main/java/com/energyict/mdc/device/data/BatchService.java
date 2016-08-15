package com.energyict.mdc.device.data;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface BatchService {

    Batch findOrCreateBatch(String name);
}
