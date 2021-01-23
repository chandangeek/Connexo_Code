package com.energyict.protocolimplv2.dlms.common.writers;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;

/**
 * This interface represents a contract with the purpose of converting message attributes into specific bytes that in the end
 * will be written into a attribute obis code. Whom will implement this interface can use a single, multiple message attributes in conversion
 * or other sources as long as they can be injected at construct time or taken from the contract defined bellow.
 */
public interface AttributeProvider {
    /**
     * @param dlmsProtocol DLMS protocol in use
     * @param message message to be used in order to read all message attributes and convert into byte[] (BER encoded).
     * @return BER encoded data that shall we written
     */
    byte[] provide(AbstractDlmsProtocol dlmsProtocol, OfflineDeviceMessage message) throws ProtocolException;

}
