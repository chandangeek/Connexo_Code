package com.energyict.mdc.device.config;

import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.ObisCode;

/**
 * Copyrights EnergyICT
 * Date: 12/11/12
 * Time: 13:19
 */
public interface LogBookSpec extends HasId {

    /**
     * Returns the object's unique id
     *
     * @return the id
     */
    public long getId();

    public DeviceConfiguration getDeviceConfiguration();

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

    void delete();

    void save();

    /**
     * Defines a Builder interface to construct a {@link LogBookSpec}
     */
    interface LogBookSpecBuilder {

        LogBookSpecBuilder setOverruledObisCode(ObisCode overruledObisCode);

        /**
         * Does final validation and <i>creates</i> the {@link LogBookSpec}
         *
         * @return the LogBookSpec
         */
        LogBookSpec add();
    }

    /**
     * Defines an <i>update</i> component to update a {@link LogBookSpec} implementation
     */
    interface LogBookSpecUpdater {

        LogBookSpecUpdater setOverruledObisCode(ObisCode overruledObisCode);

        /**
         * Updates the LogBookSpec, preferably via his DeviceConfiguration
         */
        void update();
    }
}