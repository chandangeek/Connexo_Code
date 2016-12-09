package com.energyict.mdc.upl.meterdata.identifiers;

/**
 * Enumerates the known types of LogBookIdentifier and allows for
 * inspection of the LogBookIdentifier by third party components
 * when used in combination with {@link LogBookIdentifier#getParts()}.
 *
 * @author sva
 * @since 29/10/2014 - 8:40
 */
public enum LogBookIdentifierType {
    /**
     * Indicates that {@link LogBookIdentifier#getParts()}
     * returns the numerical database identifier of the log book.
     */
    DataBaseId,

    /**
     * Indicates that {@link LogBookIdentifier#getParts()}
     * returns the DeviceIdentifier of the owning Device
     * and the ObisCode that relates to the log book.
     */
    DeviceIdentifierAndObisCode,

    ActualLogBook;

}