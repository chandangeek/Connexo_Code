package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceContext;

/**
 * Implementation of OfflineDeviceContext that indicates to need everything.
 *
 * Copyrights EnergyICT
 * Date: 14/03/13
 * Time: 14:46
 */
public enum DeviceOffline implements OfflineDeviceContext {

    needsEverything;


    @Override
    public boolean needsSlaveDevices() {
        return true;
    }

    @Override
    public boolean needsMasterLoadProfiles() {
        return true;
    }

    @Override
    public boolean needsAllLoadProfiles() {
        return true;
    }

    @Override
    public boolean needsLogBooks() {
        return true;
    }

    @Override
    public boolean needsRegisters() {
        return true;
    }

    @Override
    public boolean needsPendingMessages() {
        return true;
    }

    @Override
    public boolean needsSentMessages() {
        return true;
    }
}
