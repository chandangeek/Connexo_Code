package com.energyict.protocolimplv2.dlms.common.writers.providers;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.protocolimplv2.dlms.common.writers.AttributeProvider;

public abstract class AbstractProvider implements AttributeProvider {

    private final String attName;

    protected AbstractProvider(String attName) {
        this.attName = attName;
    }

    protected String getAttValue(OfflineDeviceMessage message) throws ProtocolException {
        return message.getDeviceMessageAttributes().stream().filter(f -> f.getName().equals(attName)).findFirst().
                orElseThrow(() -> new ProtocolException("DeviceMessage didn't contain a value found for MessageAttribute " + attName)).getValue();

    }
}
