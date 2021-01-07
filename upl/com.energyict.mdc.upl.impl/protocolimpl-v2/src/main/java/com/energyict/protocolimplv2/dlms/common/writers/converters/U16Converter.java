package com.energyict.protocolimplv2.dlms.common.writers.converters;

import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;

public class U16Converter extends AbstractConverter {

    public U16Converter(String attName) {
        super(attName);
    }

    @Override
    public byte[] convert(AbstractDlmsProtocol dlmsProtocol, OfflineDeviceMessage message) throws ProtocolException {
        return new Unsigned16(Integer.parseInt(super.getAttValue(message))).getBEREncodedByteArray();
    }
}
