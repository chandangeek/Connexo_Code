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
    void setWrappingSecurityAccessor(Optional<SecurityAccessorType> deviceSecurityAccessorType);
    SecurityAccessorType getSecurityAccessorType();
    Optional<DeviceMessageId> getKeyRenewalDeviceMessageId();
    Optional<DeviceMessageId> getServiceKeyRenewalDeviceMessageId();
    Optional<DeviceMessageSpec> getKeyRenewalDeviceMessageSpecification(boolean isService);

    List<? extends SecurityAccessorTypeKeyRenewal> getKeyRenewalAttributes();
    List<? extends SecurityAccessorTypeKeyRenewal> getServiceKeyRenewalAttributes();

    KeyRenewalBuilder newKeyRenewalBuilder(long deviceMessageIdDbValue, long serviceDeviceMessageIdDbValue);

    @ProviderType
    interface KeyRenewalBuilder {
        KeyRenewalBuilder addProperty(String key, Object value, boolean isServiceKey);
        SecurityAccessorTypeOnDeviceType add();
    }
}
