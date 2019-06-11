/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config;

import com.elster.jupiter.pki.SecurityAccessorType;

import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

@ProviderType
public interface SecurityAccessorTypeOnDeviceType {
    DeviceType getDeviceType();
    SecurityAccessorType getSecurityAccessorType();
    Optional<String> getDefaultKey();
    void setDefaultKey(String defaultKey);
}
