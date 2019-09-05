/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.config;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.protocol.DeviceMessageId;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface SecurityAccessorTypeKeyRenewal {
    DeviceMessageId getKeyRenewalDeviceMessageId();
    PropertySpec getSpecification();
    Object getValue();
    String getName();
}
