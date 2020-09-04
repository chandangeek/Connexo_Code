package com.energyict.mdc.upl.tasks.support;

import java.util.Date;

/**
 * Defines functionality to <b>change</b> the Clock of a Device
 */
public interface DeviceClockSupport extends DeviceBasicTimeSupport {

    public enum ClockChangeMode {
        SET, SYNC, FORCE
    }

    /**
     * Write the given new time to the Device.
     *
     * @param timeToSet the new time to set
     */
    void setTime(Date timeToSet);

    /**
     * Write the given new time to the Device given information about the change mode.
     * This info is useful to some device.
     *
     * @param timeToSet the new time to set
     * @param changeMode the new time to change mode (set, sync, force)
     */
    default void setTime(Date timeToSet, ClockChangeMode changeMode) {
        setTime(timeToSet);
    }

}