/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle;

import aQute.bnd.annotation.ProviderType;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroCheck;

@ProviderType
public interface DeviceLifeCycleActionViolation {

    ServerMicroCheck getCheck();

    String getLocalizedMessage();
}