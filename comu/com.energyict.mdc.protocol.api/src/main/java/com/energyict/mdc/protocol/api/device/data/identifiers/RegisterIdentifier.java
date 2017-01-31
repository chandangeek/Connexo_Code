/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device.data.identifiers;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.BaseRegister;

/**
 * Uniquely identifies a register within a physical device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-28 (17:54)
 */
public interface RegisterIdentifier {

    /**
     * Finds the {@link com.energyict.mdc.protocol.api.device.BaseRegister} that is uniquely identified by this RegisterIdentifier.
     *
     * @return The Register
     */
    public BaseRegister findRegister ();

    /**
     * Returns the ObisCode of the register which is used by the HeadEnd system.
     *
     * @return the ObisCode of the register
     */
    ObisCode getObisCode();

    /**
     * Returns the ObisCode of the register which is used by the Device.
     * Eg. this can be the same as {@link #getObisCode()} or it can be overridden.
     *
     * @return the ObisCode of the register, known by the Device
     */
    ObisCode getDeviceRegisterObisCode();

    /**
     * @return the DeviceIdentifier for this RegisterIdentifier
     */
    DeviceIdentifier<?> getDeviceIdentifier();

}