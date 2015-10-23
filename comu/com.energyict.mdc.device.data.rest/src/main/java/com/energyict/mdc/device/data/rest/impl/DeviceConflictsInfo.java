package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.config.DeviceConfigConflictMapping;

/**
 * Created by antfom on 22.10.2015.
 */
public class DeviceConflictsInfo {
    public long id;
    public boolean isSolved;

    public DeviceConflictsInfo(DeviceConfigConflictMapping deviceConfigConflictMapping) {
        this.id = deviceConfigConflictMapping.getId();
        this.isSolved = deviceConfigConflictMapping.isSolved();
    }
}
