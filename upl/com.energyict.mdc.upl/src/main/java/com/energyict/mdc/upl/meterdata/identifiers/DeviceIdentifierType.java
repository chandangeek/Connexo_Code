package com.energyict.mdc.upl.meterdata.identifiers;

/**
 * Enumerates the known types of DeviceIdentifier and allows for
 * inspection of the DeviceIdentifier by third party components
 * when used in combination with {@link DeviceIdentifier#getIdentifier()}.
 * <p/>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 1/10/2014 - 15:54
 */
public enum DeviceIdentifierType {

    /**
     * Indicates that {@link DeviceIdentifier#getIdentifier()}
     * returns the serial number of the Device.
     */
    SerialNumber,
    /**
     * Indicates that {@link DeviceIdentifier#getIdentifier()}
     * returns a grep pattern to match the serial number of the Device.
     */
    LikeSerialNumber,
    /**
     * Indicates that {@link DeviceIdentifier#getIdentifier()}
     * returns the database identifier of the Device.
     */
    DataBaseId,
    /**
     * Indicates that the {@link DeviceIdentifier#getIdentifier()}
     * returns the value of the (legacy) general property "callHomeId".
     */
    CallHomeId;

}