package com.energyict.mdc.upl.meterdata.identifiers;

/**
 * Enumerates the known types of LoadProfileIdentifiers and allows for
 * inspection of the LoadProfileIdentifier by third party components
 * when used in combination with {@link LoadProfileIdentifier#getParts()}.
 *
 * @author sva
 * @since 29/10/2014 - 8:40
 */
public enum LoadProfileIdentifierType {
    /**
     * Indicates that {@link LoadProfileIdentifier#getParts()}
     * returns the numerical database identifier of the load profile.
     */
    DataBaseId,

    /**
     * Indicates that {@link LoadProfileIdentifier#getParts()}
     * returns the DeviceIdentifier of the owning Device
     * and the ObisCode that relates to the load profile.
     */
    DeviceIdentifierAndObisCode,

    /**
     * Indicates that {@link LoadProfileIdentifier#getParts()}
     * returns the DeviceIdentifier of the owning Device
     * and it suffices to take the first load profile.
     */
    FistLoadProfileOnDevice,

    Other,

    ActualLoadProfile

}