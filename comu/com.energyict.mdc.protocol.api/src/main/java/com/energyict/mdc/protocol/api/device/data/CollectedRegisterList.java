package com.energyict.mdc.protocol.api.device.data;

import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import java.util.List;

/**
 * @author sva
 * @since 18/01/13 - 14:20
 */
public interface CollectedRegisterList extends CollectedData {

    void addCollectedRegister(CollectedRegister collectedRegister);

    List<CollectedRegister> getCollectedRegisters();

    /**
     * @return the unique identifier of the Device
     */
    DeviceIdentifier getDeviceIdentifier();

}
