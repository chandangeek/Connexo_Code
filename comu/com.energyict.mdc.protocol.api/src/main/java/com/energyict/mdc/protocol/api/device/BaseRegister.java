package com.energyict.mdc.protocol.api.device;

import com.energyict.obis.ObisCode;

/**
 * Register represents a single register in a rtu
 */
public interface BaseRegister {

    ObisCode getRegisterTypeObisCode();

    ObisCode getRegisterSpecObisCode();

    ObisCode getDeviceObisCode();

    long getRegisterSpecId();
}