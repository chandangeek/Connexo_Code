package com.energyict.mdc.protocol.device.data;

import com.energyict.mdc.protocol.inbound.DeviceIdentifier;

import java.util.List;

/**
 * @author sva
 * @since 18/01/13 - 14:20
 */
public interface CollectedRegisterList extends CollectedData {

    public void addCollectedRegister(CollectedRegister collectedRegister);

    public List<CollectedRegister> getCollectedRegisters();

    /**
     * @return the unique identifier of the Device
     */
    public DeviceIdentifier getDeviceIdentifier();

}
