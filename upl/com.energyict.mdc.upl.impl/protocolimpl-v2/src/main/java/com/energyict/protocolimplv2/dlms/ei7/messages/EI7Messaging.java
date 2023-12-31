package com.energyict.protocolimplv2.dlms.ei7.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.a2.messages.A2Messaging;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;

import java.util.Date;
import java.util.List;

public class EI7Messaging extends A2Messaging {

    private EI7MessageExecutor messageExecutor;
    private List<DeviceMessageSpec> supportedMessages;

    public EI7Messaging(AbstractDlmsProtocol protocol, PropertySpecService propertySpecService, NlsService nlsService,
                        Converter converter, DeviceMessageFileExtractor messageFileExtractor,
                        KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(protocol, propertySpecService, nlsService, converter, messageFileExtractor, keyAccessorTypeExtractor);
    }

    protected EI7MessageExecutor getMessageExecutor() {
        if (messageExecutor == null) {
            this.messageExecutor = new EI7MessageExecutor(getProtocol(), getProtocol().getCollectedDataFactory(), getProtocol().getIssueFactory(), keyAccessorTypeExtractor);
        }
        return messageExecutor;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        if (supportedMessages == null) {
            supportedMessages = super.getSupportedMessages();
            // Not available for EI7
            supportedMessages.remove(NetworkConnectivityMessage.CHANGE_GPRS_IP_ADDRESS_AND_PORT.get(getPropertySpecService(), getNlsService(), getConverter()));
            supportedMessages.remove(NetworkConnectivityMessage.CONFIGURE_AUTO_CONNECT_A2.get(getPropertySpecService(), getNlsService(), getConverter()));
            supportedMessages.remove(ContactorDeviceMessage.CHANGE_VALVE_ENABLE_PASSWORD.get(getPropertySpecService(), getNlsService(), getConverter()));
            supportedMessages.remove(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE.get(getPropertySpecService(), getNlsService(), getConverter()));
            supportedMessages.remove(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE.get(getPropertySpecService(), getNlsService(), getConverter()));
            supportedMessages.remove(ContactorDeviceMessage.CONTACTOR_CLOSE_AND_CLOSE_INVOICING_PERIOD_WITH_ACTIVATION_DATE.get(getPropertySpecService(), getNlsService(), getConverter()));

            supportedMessages.add(NetworkConnectivityMessage.CHANGE_PUSH_SCHEDULER.get(getPropertySpecService(), getNlsService(), getConverter()));
            supportedMessages.add(NetworkConnectivityMessage.CHANGE_PUSH_SETUP.get(getPropertySpecService(), getNlsService(), getConverter()));
            supportedMessages.add(NetworkConnectivityMessage.CHANGE_ORPHAN_STATE_THRESHOLD.get(getPropertySpecService(), getNlsService(), getConverter()));
            supportedMessages.add(NetworkConnectivityMessage.CHANGE_NETWORK_TIMEOUT.get(getPropertySpecService(), getNlsService(), getConverter()));
            supportedMessages.add(SecurityMessage.KEY_RENEWAL_EI6_7.get(getPropertySpecService(), getNlsService(), getConverter()));
            supportedMessages.add(ConfigurationChangeDeviceMessage.GAS_DAY_CONFIGURATION.get(getPropertySpecService(), getNlsService(), getConverter()));
        }
        return supportedMessages;
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        final String propertyName = propertySpec.getName();
        if (propertyName.equals(DeviceMessageConstants.executionTime) ||
                propertyName.equals(DeviceMessageConstants.communicationWindowStartTime) ||
                propertyName.equals(DeviceMessageConstants.communicationWindowStopTime) ||
                propertyName.equals(DeviceMessageConstants.startOfConventionalGasDay)) {
            return String.valueOf(((Date) messageAttribute).getTime());
        }
        return super.format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
    }
}