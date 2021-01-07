package com.energyict.protocolimplv2.dlms.common.writers;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;

public interface MessageAttributeConverter {
    /**
     * @param dlmsProtocol DLMS protocol in use
     * @param message message to be used in order to read all message attributes and convert into byte[] (BER encoded)
     * @return BER encoded data that shall we written
     */
    byte[] convert(AbstractDlmsProtocol dlmsProtocol, OfflineDeviceMessage message) throws ProtocolException;

}
