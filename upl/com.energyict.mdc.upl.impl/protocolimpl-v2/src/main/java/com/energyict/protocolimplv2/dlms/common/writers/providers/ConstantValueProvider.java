package com.energyict.protocolimplv2.dlms.common.writers.providers;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.common.writers.AttributeProvider;

public class ConstantValueProvider implements AttributeProvider {

    private final AbstractDataType abstractDataType;

    public ConstantValueProvider(AbstractDataType abstractDataType) {
        this.abstractDataType = abstractDataType;
    }

    @Override
    public byte[] provide(AbstractDlmsProtocol dlmsProtocol, OfflineDeviceMessage message) {
        return this.abstractDataType.getBEREncodedByteArray();
    }
}
