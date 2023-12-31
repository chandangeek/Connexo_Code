package com.energyict.protocolimplv2.dlms.ei6v2021.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.ei7.messages.EI7Messaging;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;

import java.util.List;

public class EI6v2021Messaging extends EI7Messaging {

    private List<DeviceMessageSpec> supportedMessages;

    public EI6v2021Messaging(AbstractDlmsProtocol protocol, PropertySpecService propertySpecService, NlsService nlsService,
                             Converter converter, DeviceMessageFileExtractor messageFileExtractor,
                             KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(protocol, propertySpecService, nlsService, converter, messageFileExtractor, keyAccessorTypeExtractor);
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        if (supportedMessages == null) {
            supportedMessages = super.getSupportedMessages();

            // Add again removed messages from EI7 for EI6 backwards compatibility
            supportedMessages.add(NetworkConnectivityMessage.CHANGE_GPRS_IP_ADDRESS_AND_PORT.get(getPropertySpecService(), getNlsService(), getConverter()));
            supportedMessages.add(NetworkConnectivityMessage.CONFIGURE_AUTO_CONNECT_A2.get(getPropertySpecService(), getNlsService(), getConverter()));
            supportedMessages.add(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE.get(getPropertySpecService(), getNlsService(), getConverter()));
            supportedMessages.add(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE.get(getPropertySpecService(), getNlsService(), getConverter()));
            supportedMessages.add(ContactorDeviceMessage.CONTACTOR_CLOSE_AND_CLOSE_INVOICING_PERIOD_WITH_ACTIVATION_DATE.get(getPropertySpecService(), getNlsService(), getConverter()));
            supportedMessages.add(ContactorDeviceMessage.CHANGE_VALVE_ENABLE_PASSWORD.get(getPropertySpecService(), getNlsService(), getConverter()));
        }
        return supportedMessages;
    }
}
