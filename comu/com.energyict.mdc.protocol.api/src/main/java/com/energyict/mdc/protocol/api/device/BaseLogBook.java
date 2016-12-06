package com.energyict.mdc.protocol.api.device;

import com.energyict.obis.ObisCode;

/**
 * Copyrights EnergyICT
 * Date: 28/11/12
 * Time: 10:02
 */
public interface BaseLogBook {

    ObisCode getDeviceObisCode();

    long getId();
}