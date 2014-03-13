package com.energyict.protocolimplv2.identifiers;

import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;

/**
 * Provides an implementation for the {@link DeviceIdentifier} interface
 * that uses a PlaceHolder for a {@link com.energyict.mdc.protocol.api.device.BaseDevice}'s serial number to uniquely identify it.
 * <b>Be aware that the serialNumber is NOT a unique field in the database.
 * It is possible that multiple devices are found based on the provided SerialNumber.
 * In that case, a {@link com.energyict.mdc.common.NotFoundException} is throw</b>
 * <p/>
 * Copyrights EnergyICT
 * Date: 9/3/13
 * Time: 11:45 AM
 */
public class DeviceIdentifierBySerialNumberPlaceHolder extends DeviceIdentifierBySerialNumber {

    private final SerialNumberPlaceHolder serialNumberPlaceHolder;

    public DeviceIdentifierBySerialNumberPlaceHolder(SerialNumberPlaceHolder serialNumberPlaceHolder) {
        super(serialNumberPlaceHolder.getSerialNumber());
        this.serialNumberPlaceHolder = serialNumberPlaceHolder;
    }

    @Override
    public String getIdentifier() {
        return serialNumberPlaceHolder.getSerialNumber();
    }

}