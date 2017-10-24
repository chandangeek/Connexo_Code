/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.config;

import com.elster.jupiter.pki.SecurityAccessorType;

/**
 * Specific interface with features for {@link SecurityAccessorType}s in the device-realm
 */
public interface DeviceSecurityAccessorType extends SecurityAccessorType {
    boolean currentUserIsAllowedToEditDeviceProperties();
    boolean currentUserIsAllowedToViewDeviceProperties();
}
