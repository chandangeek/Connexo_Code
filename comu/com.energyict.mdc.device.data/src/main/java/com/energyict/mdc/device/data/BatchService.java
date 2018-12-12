/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface BatchService {

    Batch findOrCreateBatch(String name);
}
