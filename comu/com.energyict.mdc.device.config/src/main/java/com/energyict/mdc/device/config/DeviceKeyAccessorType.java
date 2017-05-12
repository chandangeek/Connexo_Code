/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.config;

import com.elster.jupiter.pki.KeyAccessorType;

/**
 * Specific interface with features for {@link KeyAccessorType}s in the device-realm
 */
public interface DeviceKeyAccessorType extends KeyAccessorType {
    boolean currentUserIsAllowedToEditDeviceProperties();
    boolean currentUserIsAllowedToViewDeviceProperties();
}
