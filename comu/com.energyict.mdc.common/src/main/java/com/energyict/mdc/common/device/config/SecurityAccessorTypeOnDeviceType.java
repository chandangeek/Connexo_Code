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
    DeviceSecurityAccessorType getDeviceSecurityAccessorType();
    SecurityAccessorType getSecurityAccessorType();
    Optional<DeviceMessageId> getKeyRenewalDeviceMessageId();
    Optional<DeviceMessageSpec> getKeyRenewalDeviceMessageSpecification();

    List<? extends SecurityAccessorTypeKeyRenewal> getKeyRenewalAttributes();

    void resetKeyRenewal();
    KeyRenewalBuilder newKeyRenewalBuilder(DeviceMessageId deviceMessageId);

    @ProviderType
    interface KeyRenewalBuilder {
        KeyRenewalBuilder addProperty(String key, Object value);
        SecurityAccessorTypeOnDeviceType add();
    }
}
