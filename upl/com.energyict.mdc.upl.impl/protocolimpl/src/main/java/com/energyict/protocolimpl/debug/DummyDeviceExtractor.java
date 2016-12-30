package com.energyict.protocolimpl.debug;

import com.energyict.mdc.upl.messages.legacy.DeviceExtractor;
import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.meterdata.Device;

import com.energyict.obis.ObisCode;

import java.util.Optional;

/**
 * Provides an dummy implementation for the {@link Extractor} interface
 * that returns empty values in all of its methods.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-28 (09:49)
 */
final class DummyDeviceExtractor implements DeviceExtractor {
    @Override
    public String serialNumber(Device device) {
        return "";
    }

    @Override
    public Optional<com.energyict.mdc.upl.meterdata.Register> register(Device device, ObisCode obisCode) {
        return Optional.empty();
    }

}