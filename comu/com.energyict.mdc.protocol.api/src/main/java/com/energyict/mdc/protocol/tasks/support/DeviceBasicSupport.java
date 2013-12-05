package com.energyict.mdc.protocol.tasks.support;

/**
 * Defines functionality which is by default supported by a Device. This functionality will not always be fetched.
 */
public interface DeviceBasicSupport extends DeviceBasicTimeSupport {

    /**
     * @return the SerialNumber of a Device
     */
    public String getSerialNumber();

}
