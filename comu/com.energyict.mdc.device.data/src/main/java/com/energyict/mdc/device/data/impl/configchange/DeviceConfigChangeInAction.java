/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.configchange;

import com.elster.jupiter.util.HasId;

/**
 * Object which serves as a 'business lock' when a DeviceConfigChange is currently happening
 */
public interface DeviceConfigChangeInAction extends HasId {

    /**
     * Indication for self destruction.
     */
    void remove();
}
