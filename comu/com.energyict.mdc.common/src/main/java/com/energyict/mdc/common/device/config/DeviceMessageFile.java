/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.config;

import java.time.Instant;

/**
 * Extends the definition of a DeviceMessageFile as provided by the protocol api bundle
 * to add information that is known/defined by this bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-23 (12:48)
 */
public interface DeviceMessageFile extends com.energyict.mdc.common.protocol.DeviceMessageFile {
    DeviceType getDeviceType();

    Instant getCreateTime();

    long getSize();
}