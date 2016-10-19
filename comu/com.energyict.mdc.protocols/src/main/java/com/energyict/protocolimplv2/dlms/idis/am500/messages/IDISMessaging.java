package com.energyict.protocolimplv2.dlms.idis.am500.messages;


import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
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
public class IDISMessaging extends AbstractDlmsMessaging implements DeviceMessageSupport {

    protected List<DeviceMessageSpec> supportedMessages;

    protected IDISMessageExecutor messageExecutor;

    public IDISMessaging(AbstractDlmsProtocol protocol) {
        super(protocol);
    }

    protected IDISMessageExecutor getMessageExecutor() {
        if (messageExecutor == null) {
            this.messageExecutor = new IDISMessageExecutor(getProtocol(),getProtocol().getIssueService(), getProtocol().getReadingTypeUtilService(), getProtocol().getCollectedDataFactory());
        }
        return messageExecutor;
    }

    @Override
    public Set<DeviceMessageId> getSupportedMessages() {
        return Collections.emptySet();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getProtocol().getCollectedDataFactory().createCollectedMessageList(pendingMessages);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return getProtocol().getCollectedDataFactory().createCollectedMessageList(sentMessages);
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return "";
    }

}