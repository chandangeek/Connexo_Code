package com.energyict.protocolimplv2.dlms.common.writers.converters;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;

import java.io.IOException;

public class OctetStringConverter extends AbstractConverter {

    private final int length;

    public OctetStringConverter(String attName, int length) {
        super(attName);
        this.length = length;
    }

    @Override
    public byte[] convert(AbstractDlmsProtocol dlmsProtocol, OfflineDeviceMessage message) throws ProtocolException {
        try {
            return new OctetString(ProtocolTools.getBytesFromHexString(super.getAttValue(message)), length).getBEREncodedByteArray();
        } catch (IOException e) {
            throw new ProtocolException(e);
        }
    }
}
