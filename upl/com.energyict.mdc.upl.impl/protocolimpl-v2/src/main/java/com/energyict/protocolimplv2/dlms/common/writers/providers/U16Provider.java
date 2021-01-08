package com.energyict.protocolimplv2.dlms.common.writers.providers;

import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;

public class U16Provider extends AbstractProvider {

    public U16Provider(String attName) {
        super(attName);
    }

    @Override
    public byte[] provide(AbstractDlmsProtocol dlmsProtocol, OfflineDeviceMessage message) throws ProtocolException {
        return new Unsigned16(Integer.parseInt(super.getAttValue(message))).getBEREncodedByteArray();
    }
}
