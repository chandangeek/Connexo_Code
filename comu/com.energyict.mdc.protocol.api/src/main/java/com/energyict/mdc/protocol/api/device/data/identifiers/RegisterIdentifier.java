package com.energyict.mdc.protocol.api.device.data.identifiers;

import com.energyict.mdc.common.ObisCode;

/**
 * Uniquely identifies a register within a physical device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-28 (17:54)
 */
public interface RegisterIdentifier {


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
}