package com.energyict.mdc.protocol.api.inbound;

/**
 * Enumerates the known types of MessageIdentifiers.<BR>
 * This can be used to know more about the identifier without having to resolve it using database access.
 * <p/>
 *
 * @author sva
 * @since 29/10/2014 - 8:40
 */
public enum MessageIdentifierType {

    DataBaseId,
    DeviceIdentifierAndProtocolInfoParts,
    Other
}
