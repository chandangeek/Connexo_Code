package com.energyict.protocolimplv2.dlms.ei7.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.a2.messages.A2Messaging;
import com.energyict.protocolimplv2.messages.GeneralDeviceMessage;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;

import java.util.List;

public class EI7Messaging extends A2Messaging {

    private EI7MessageExecutor messageExecutor;
    private List<DeviceMessageSpec> supportedMessages;

    public EI7Messaging(AbstractDlmsProtocol protocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, DeviceMessageFileExtractor messageFileExtractor) {
        super(protocol, propertySpecService, nlsService, converter, messageFileExtractor);
    }

    protected EI7MessageExecutor getMessageExecutor() {
        if (messageExecutor == null) {
            this.messageExecutor = new EI7MessageExecutor(getProtocol(), getProtocol().getCollectedDataFactory(), getProtocol().getIssueFactory());
        }
        return messageExecutor;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        if (supportedMessages == null) {
            supportedMessages = super.getSupportedMessages();
            supportedMessages.add(NetworkConnectivityMessage.WRITE_PUSH_SCHEDULER.get(getPropertySpecService(), getNlsService(), getConverter()));
            supportedMessages.add(NetworkConnectivityMessage.CONFIGURE_PUSH_SETUP_EI7.get(getPropertySpecService(), getNlsService(), getConverter()));
            supportedMessages.add(NetworkConnectivityMessage.WRITE_ORPHAN_STATE.get(getPropertySpecService(), getNlsService(), getConverter()));
        }
        return supportedMessages;
    }
}
