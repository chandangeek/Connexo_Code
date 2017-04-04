package com.energyict.mdc.upl.meterdata;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import com.energyict.obis.ObisCode;

public interface Register {
    DeviceIdentifier getDeviceIdentifier();
    ObisCode getObisCode();
}