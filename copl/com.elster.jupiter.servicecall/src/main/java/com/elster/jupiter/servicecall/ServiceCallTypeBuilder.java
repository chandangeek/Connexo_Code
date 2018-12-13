/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface ServiceCallTypeBuilder {
    ServiceCallTypeBuilder logLevel(LogLevel logLevel);

    ServiceCallTypeBuilder customPropertySet(RegisteredCustomPropertySet customPropertySet);

    ServiceCallType create();

    ServiceCallTypeBuilder handler(String serviceCallHandler);
}
