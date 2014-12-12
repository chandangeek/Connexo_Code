package com.energyict.protocolimplv2.elster.garnet;


import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.tasks.support.DeviceMessageSupport;
import com.energyict.protocols.exception.UnsupportedMethodException;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * @author sva
 * @since 27/06/2014 - 13:26
 */
public class EMeterMessaging implements DeviceMessageSupport {

    private final A100C deviceProtocol;

    public EMeterMessaging(A100C deviceProtocol) {
        this.deviceProtocol = deviceProtocol;
    }

    @Override
    public Set<DeviceMessageId> getSupportedMessages() {
        return EnumSet.of(DeviceMessageId.CONTACTOR_CLOSE, DeviceMessageId.CONTACTOR_OPEN);
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return messageAttribute.toString();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        throw new UnsupportedMethodException(this.getClass(), "executePendingMessages");
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        throw new UnsupportedMethodException(this.getClass(), "updateSentMessages");
    }

    public A100C getDeviceProtocol() {
        return deviceProtocol;
    }
}