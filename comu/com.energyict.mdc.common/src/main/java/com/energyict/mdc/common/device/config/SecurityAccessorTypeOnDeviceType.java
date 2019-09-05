/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.config;

import com.elster.jupiter.pki.SecurityAccessorType;
import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.common.protocol.DeviceMessageSpec;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Optional;

@ProviderType
public interface SecurityAccessorTypeOnDeviceType {
    DeviceType getDeviceType();
    SecurityAccessorType getSecurityAccessorType();
    Optional<String> getDefaultKey();
    void setDefaultKey(String defaultKey);
    Optional<DeviceMessageId> getKeyRenewalDeviceMessageId();
    Optional<DeviceMessageSpec> getKeyRenewalDeviceMessageSpecification();

    List<? extends SecurityAccessorTypeKeyRenewal> getKeyRenewalAttributes();
}
