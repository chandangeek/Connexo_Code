package com.energyict.protocolimplv2.dlms.common.writers.providers;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;

import java.io.IOException;

public class OctetStringFromHexProvider extends AbstractProvider {

    private final int length;

    public OctetStringFromHexProvider(String attName, int length) {
        super(attName);
        this.length = length;
    }

    @Override
    public byte[] provide(AbstractDlmsProtocol dlmsProtocol, OfflineDeviceMessage message) throws ProtocolException {
        try {
            return  OctetString.fromByteArray(ProtocolTools.getBytesFromHexString(super.getAttValue(message), 1), length).getBEREncodedByteArray();
        } catch (IOException e) {
            throw new ProtocolException(e);
        }
    }
}
