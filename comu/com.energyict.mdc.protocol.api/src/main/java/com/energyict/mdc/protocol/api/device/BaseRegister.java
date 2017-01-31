/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device;

import com.energyict.mdc.common.ObisCode;

/**
 * Register represents a single register in a rtu
 */
public interface BaseRegister {

    ObisCode getRegisterTypeObisCode();

    ObisCode getRegisterSpecObisCode();

    ObisCode getDeviceObisCode();

    long getRegisterSpecId();
}