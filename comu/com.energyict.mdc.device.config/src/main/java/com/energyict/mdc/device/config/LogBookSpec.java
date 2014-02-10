package com.energyict.mdc.device.config;

import com.energyict.mdc.common.ObisCode;

/**
 * Copyrights EnergyICT
 * Date: 12/11/12
 * Time: 13:19
 */
public interface LogBookSpec {

    /**
     * Returns the object's unique id
     *
     * @return the id
     */
    public long getId();

    public DeviceConfiguration getDeviceConfig();

    public LogBookType getLogBookType();

    /**
     * Returns the ObisCode of this LogBook which is configured/overwritten in the DeviceConfiguration.
     * <b>If no ObisCode was overrule, then the ObisCode of the spec is returned.</b>
     *
     * @return the overruled ObisCode of this LogBook
     * @see #getObisCode()
     */
    public ObisCode getDeviceObisCode();

    /**
     * Returns the ObisCode of this LogBook which is configured/overwritten in the DeviceConfiguration.
     *
     * @return the ObisCode of this LogBook
     * @see #getDeviceObisCode()
     */
    public ObisCode getObisCode();

    void setDeviceConfiguration(DeviceConfiguration deviceConfiguration);

    void setLogBookType(LogBookType logBookType);

    void setOverruledObisCode(ObisCode overruledObisCode);

    void validateDelete();
}