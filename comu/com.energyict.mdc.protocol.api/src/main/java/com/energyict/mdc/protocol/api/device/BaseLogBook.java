/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device;

import com.energyict.mdc.common.ObisCode;

public interface BaseLogBook {

    ObisCode getDeviceObisCode();

    long getId();
}