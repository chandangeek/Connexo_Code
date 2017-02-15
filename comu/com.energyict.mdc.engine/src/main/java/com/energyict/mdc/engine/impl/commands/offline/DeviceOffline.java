/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.offline;

import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceContext;

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

    @Override
    public boolean needsFirmwareVersions() {
        return true;
    }

    @Override
    public boolean needsTouCalendar() {
        return true;
    }


}