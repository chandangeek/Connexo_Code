/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.config;

import com.elster.jupiter.util.HasId;
import com.energyict.mdc.common.masterdata.LogBookType;

import aQute.bnd.annotation.ConsumerType;
import com.energyict.obis.ObisCode;

@ConsumerType
public interface LogBookSpec extends HasId {

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

    void setOverruledObisCode(ObisCode overruledObisCode);

    void validateDelete();

    void delete();

    void save();

    long getVersion();

    /**
     * Defines a Builder interface to construct a {@link LogBookSpec}.
     */
    interface LogBookSpecBuilder {

        LogBookSpecBuilder setOverruledObisCode(ObisCode overruledObisCode);

        /**
         * Does final validation and <i>creates</i> the {@link LogBookSpec}.
         *
         * @return the LogBookSpec
         */
        LogBookSpec add();
    }

    /**
     * Defines an <i>update</i> component to update a {@link LogBookSpec} implementation.
     */
    interface LogBookSpecUpdater {

        LogBookSpecUpdater setOverruledObisCode(ObisCode overruledObisCode);

        /**
         * Updates the LogBookSpec.
         */
        void update();
    }

}