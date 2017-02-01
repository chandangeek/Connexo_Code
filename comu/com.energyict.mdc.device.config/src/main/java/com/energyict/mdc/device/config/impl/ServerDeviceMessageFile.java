/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceMessageFile;

import java.time.Instant;

/**
 * Adds behavior to {@link DeviceMessageFile} that is reserved
 * for server-side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-07-15 (10:33)
 */
public interface ServerDeviceMessageFile extends DeviceMessageFile {
    void setObsolete(Instant instant);

    boolean isObsolete();
}