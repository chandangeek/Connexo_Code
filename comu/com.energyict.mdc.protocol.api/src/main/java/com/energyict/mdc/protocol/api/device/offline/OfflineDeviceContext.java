package com.energyict.mdc.protocol.api.device.offline;

/**
 * Copyrights EnergyICT
 * Date: 14/03/13
 * Time: 14:42
 */
public interface OfflineDeviceContext {

    boolean needsSlaveDevices();

    boolean needsMasterLoadProfiles();

    boolean needsAllLoadProfiles();

    boolean needsLogBooks();

    boolean needsRegisters();

    boolean needsPendingMessages();

    boolean needsSentMessages();

    boolean needsFirmwareVersions();
}
