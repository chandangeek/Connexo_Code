package com.energyict.protocolimplv2.dlms.idis.am500.messages.mbus;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.tasks.support.DeviceMessageSupport;

import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractDlmsMessaging;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 6/01/2015 - 15:34
 */
public class IDISMBusMessaging extends AbstractDlmsMessaging implements DeviceMessageSupport {

    public IDISMBusMessaging(AbstractDlmsProtocol protocol) {
        super(protocol);
    }

    @Override
    public Set<DeviceMessageId> getSupportedMessages() {
        return Collections.emptySet();
    }

    /**
     * Not supported here, message(s) will be executed in the e-meter protocol
     */
    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getProtocol().getCollectedDataFactory().createCollectedMessageList(pendingMessages);
    }

    /**
     * Not supported here, message(s) will be executed in the e-meter protocol
     */
    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> offlineDeviceMessages) {
        return getProtocol().getCollectedDataFactory().createCollectedMessageList(offlineDeviceMessages);
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return "";
    }
}