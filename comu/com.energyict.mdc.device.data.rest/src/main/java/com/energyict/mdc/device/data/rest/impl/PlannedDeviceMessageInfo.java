package com.energyict.mdc.device.data.rest.impl;

public class PlannedDeviceMessageInfo {
    public boolean willBePickedUpByPlannedComtask;
    public boolean willBePickedUpByComtask;

    public PlannedDeviceMessageInfo(boolean willBePickedUpByPlannedComtask, boolean willBePickedUpByComtask) {
        this.willBePickedUpByPlannedComtask = willBePickedUpByPlannedComtask;
        this.willBePickedUpByComtask = willBePickedUpByComtask;
    }
}
