package com.energyict.mdc.upl.meterdata.identifiers;

/**
 * Enumerates the known types of MessageIdentifier and allows for
 * inspection of the MessageIdentifier by third party components
 * when used in combination with {@link MessageIdentifier#getParts()}.
 * <p/>
 *
 * @author sva
 * @since 29/10/2014 - 8:40
 */
public enum MessageIdentifierType {
    /**
     * Indicates that {@link MessageIdentifier#getParts()}
     * returns the numerical database identifier of the message.
     */
    DataBaseId,
    DeviceIdentifierAndProtocolInfoParts;

}