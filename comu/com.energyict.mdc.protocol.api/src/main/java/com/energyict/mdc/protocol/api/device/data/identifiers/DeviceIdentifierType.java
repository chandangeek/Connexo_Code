package com.energyict.mdc.protocol.api.device.data.identifiers;

/**
 * Enumerates the known types of DeviceIdentifiers.<BR>
 * This can be used to know more about the identifier without having to resolve it using database access.
 * <p/>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 1/10/2014 - 15:54
 */
public enum DeviceIdentifierType {

    SerialNumber,
    DataBaseId,
    CallHomeId,
    Other,
    ActualDevice
}