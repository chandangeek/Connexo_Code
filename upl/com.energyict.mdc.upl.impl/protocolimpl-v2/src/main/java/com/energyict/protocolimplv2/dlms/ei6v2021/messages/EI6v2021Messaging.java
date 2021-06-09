package com.energyict.protocolimplv2.dlms.ei6v2021.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.ei7.messages.EI7Messaging;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;

import java.util.List;

public class EI6v2021Messaging extends EI7Messaging {

    private List<DeviceMessageSpec> supportedMessages;

    public EI6v2021Messaging(AbstractDlmsProtocol protocol, PropertySpecService propertySpecService, NlsService nlsService,
                             Converter converter, DeviceMessageFileExtractor messageFileExtractor) {
        super(protocol, propertySpecService, nlsService, converter, messageFileExtractor);
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        if (supportedMessages == null) {
            supportedMessages = super.getSupportedMessages();

            // Add again removed messages from EI7 for EI6 backwards compatibility
            supportedMessages.add(NetworkConnectivityMessage.CHANGE_GPRS_IP_ADDRESS_AND_PORT.get(getPropertySpecService(), getNlsService(), getConverter()));
            supportedMessages.add(NetworkConnectivityMessage.CONFIGURE_AUTO_CONNECT_A2.get(getPropertySpecService(), getNlsService(), getConverter()));
        }
        return supportedMessages;
    }
}
