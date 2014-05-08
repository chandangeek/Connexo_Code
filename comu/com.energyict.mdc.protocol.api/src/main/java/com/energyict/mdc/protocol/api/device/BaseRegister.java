package com.energyict.mdc.protocol.api.device;

import com.energyict.mdc.common.ObisCode;

/**
 * Register represents a single register in a rtu
 */
public interface BaseRegister {

    ObisCode getRegisterMappingObisCode();

    ObisCode getRegisterSpecObisCode();

    ObisCode getDeviceObisCode();

    long getRegisterSpecId();
}