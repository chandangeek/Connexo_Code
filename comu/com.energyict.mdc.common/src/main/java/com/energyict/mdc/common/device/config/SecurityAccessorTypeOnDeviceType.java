/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.config;

import aQute.bnd.annotation.ConsumerType;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.common.protocol.DeviceMessageSpec;

import java.util.List;
import java.util.Optional;

@ConsumerType
public interface SecurityAccessorTypeOnDeviceType {
    DeviceType getDeviceType();
    DeviceSecurityAccessorType getDeviceSecurityAccessorType();
    void setWrappingSecurityAccessor(Optional<SecurityAccessorType> deviceSecurityAccessorType);
    SecurityAccessorType getSecurityAccessorType();
    Optional<DeviceMessageId> getKeyRenewalDeviceMessageId();
    Optional<DeviceMessageId> getServiceKeyRenewalDeviceMessageId();
    Optional<DeviceMessageSpec> getKeyRenewalDeviceMessageSpecification();
    Optional<DeviceMessageSpec> getServiceKeyRenewalDeviceMessageSpecification();

    List<? extends SecurityAccessorTypeKeyRenewal> getKeyRenewalAttributes();
    List<? extends SecurityAccessorTypeKeyRenewal> getServiceKeyRenewalAttributes();

    void resetKeyRenewal();
    KeyRenewalBuilder newKeyRenewalBuilder(DeviceMessageId deviceMessageId, DeviceMessageId serviceDeviceMessageId);

    boolean isRenewalConfigured();

    @ConsumerType
    interface KeyRenewalBuilder {
        KeyRenewalBuilder addProperty(String key, Object value, boolean isServiceKey);
        SecurityAccessorTypeOnDeviceType add();
    }
}
