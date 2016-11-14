package com.energyict.mdc.upl.meterdata.identifiers;

/**
 * Enumerates the known types of RegisterIdentifier and allows for
 * inspection of the RegisterIdentifierType by third party components
 * when used in combination with {@link RegisterIdentifier#getParts()}.
 *
 * @author sva
 * @since 29/10/2014 - 8:39
 */
public enum RegisterIdentifierType {
    /**
     * Indicates that {@link RegisterIdentifier#getParts()}
     * returns the numerical database identifier of the register.
     */
    DataBaseId,

    /**
     * Indicates that {@link RegisterIdentifier#getParts()}
     * returns the DeviceIdentifier of the owning Device
     * and the ObisCode that relates to the register.
     */
    DeviceIdentifierAndObisCode,

    PrimeRegisterForChannel;

}