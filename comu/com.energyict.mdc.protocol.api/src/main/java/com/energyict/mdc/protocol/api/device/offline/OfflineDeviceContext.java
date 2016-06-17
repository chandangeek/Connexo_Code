package com.energyict.mdc.protocol.api.device.offline;

import aQute.bnd.annotation.ProviderType;

/**
 * Copyrights EnergyICT
 * Date: 14/03/13
 * Time: 14:42
 */
@ProviderType
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
