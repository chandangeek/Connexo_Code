/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl;

import com.energyict.mdc.device.data.Device;

import java.time.Instant;

public class TransitionDoneEventInfo {

    private long device;
    private long lifecycle;
    private long modTime;

    public long getDevice() {
        return device;
    }

    public void setDevice(long device) {
        this.device = device;
    }

    public long getLifecycle() {
        return lifecycle;
    }

    public void setLifecycle(long lifecycle) {
        this.lifecycle = lifecycle;
    }

    public long getModTime() {
        return modTime;
    }

    public void setModTime(long modTime) {
        this.modTime = modTime;
    }

    public static TransitionDoneEventInfo forDevice(Device device, Instant modTime) {
        TransitionDoneEventInfo eventInfo = new TransitionDoneEventInfo();
        eventInfo.setDevice(device.getId());
        eventInfo.setLifecycle(device.getDeviceType().getDeviceLifeCycle().getId());
        eventInfo.setModTime(modTime.toEpochMilli());
        return eventInfo;
    }

}
