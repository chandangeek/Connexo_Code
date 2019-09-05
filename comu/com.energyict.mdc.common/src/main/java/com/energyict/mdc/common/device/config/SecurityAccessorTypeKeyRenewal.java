/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.config;

import com.elster.jupiter.properties.PropertySpec;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface SecurityAccessorTypeKeyRenewal {
    PropertySpec getSpecification();
    Object getValue();
    String getName();
    void setName(String name);
    void setValue(String value);
}
