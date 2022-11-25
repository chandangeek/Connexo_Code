package com.energyict.protocolimplv2.dlms.a2.messages;

import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.KeyAccessorType;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;

import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.messages.ClockDeviceMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractDlmsMessaging;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class A2Messaging extends AbstractDlmsMessaging implements DeviceMessageSupport {

    private List<DeviceMessageSpec> supportedMessages;

    private A2MessageExecutor messageExecutor;
    private final DeviceMessageFileExtractor messageFileExtractor;
    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;
    private final Converter converter;
    protected final KeyAccessorTypeExtractor keyAccessorTypeExtractor;

    public A2Messaging(AbstractDlmsProtocol protocol, PropertySpecService propertySpecService, NlsService nlsService,
                       Converter converter, DeviceMessageFileExtractor messageFileExtractor,
                       KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(protocol);
        this.messageFileExtractor = messageFileExtractor;
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
        this.converter = converter;
        this.keyAccessorTypeExtractor = keyAccessorTypeExtractor;
    }

    protected A2MessageExecutor getMessageExecutor() {
        if (messageExecutor == null) {
            this.messageExecutor = new A2MessageExecutor(getProtocol(), getProtocol().getCollectedDataFactory(), getProtocol().getIssueFactory());
        }
        return messageExecutor;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        if (supportedMessages == null) {
            supportedMessages = new ArrayList<>();
            supportedMessages.add(ClockDeviceMessage.SET_TIMEZONE_OFFSET.get(this.propertySpecService, this.nlsService, this.converter));
            supportedMessages.add(ClockDeviceMessage.SyncTime.get(this.propertySpecService, this.nlsService, this.converter));
            supportedMessages.add(ClockDeviceMessage.ConfigureDST.get(this.propertySpecService, this.nlsService, this.converter));

            supportedMessages.add(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_KDL_AND_HASH_AND_ACTIVATION.get(this.propertySpecService, this.nlsService, this.converter));
            supportedMessages.add(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE.get(this.propertySpecService, this.nlsService, this.converter));
            supportedMessages.add(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE.get(this.propertySpecService, this.nlsService, this.converter));
            supportedMessages.add(ContactorDeviceMessage.CONTACTOR_CLOSE_AND_CLOSE_INVOICING_PERIOD_WITH_ACTIVATION_DATE.get(this.propertySpecService, this.nlsService, this.converter));
            supportedMessages.add(ContactorDeviceMessage.CHANGE_VALVE_ENABLE_PASSWORD.get(this.propertySpecService, this.nlsService, this.converter));

            supportedMessages.add(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS.get(this.propertySpecService, this.nlsService, this.converter));
            supportedMessages.add(NetworkConnectivityMessage.CHANGE_GPRS_IP_ADDRESS_AND_PORT.get(this.propertySpecService, this.nlsService, this.converter));
            supportedMessages.add(NetworkConnectivityMessage.CONFIGURE_AUTO_CONNECT_A2.get(this.propertySpecService, this.nlsService, this.converter));
            supportedMessages.add(NetworkConnectivityMessage.CHANGE_INACTIVITY_TIMEOUT.get(this.propertySpecService, this.nlsService, this.converter));
            supportedMessages.add(NetworkConnectivityMessage.ChangeSessionTimeout.get(this.propertySpecService, this.nlsService, this.converter));
            supportedMessages.add(NetworkConnectivityMessage.CHANGE_SIM_PIN.get(this.propertySpecService, this.nlsService, this.converter));
            supportedMessages.add(NetworkConnectivityMessage.CHANGE_NBIOT_APN_CREDENTIALS.get(getPropertySpecService(), getNlsService(), getConverter()));

            supportedMessages.add(SecurityMessage.CHANGE_HLS_SECRET_PASSWORD_FOR_CLIENT.get(this.propertySpecService, this.nlsService, this.converter));
        }
        return supportedMessages;
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getMessageExecutor().executePendingMessages(pendingMessages);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return getMessageExecutor().updateSentMessages(sentMessages);
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(DeviceMessageConstants.firmwareUpdateActivationDateAttributeName)) {
            return String.valueOf(((Date) messageAttribute).getTime());
        } else if (propertySpec.getName().equals(DeviceMessageConstants.passwordAttributeName)) {
            return this.keyAccessorTypeExtractor.passiveValueContent((KeyAccessorType) messageAttribute);
        } else if (propertySpec.getName().equals(DeviceMessageConstants.firmwareUpdateFileAttributeName)) {
            return messageAttribute.toString();
        } else if (propertySpec.getName().equals(DeviceMessageConstants.contactorActivationDateAttributeName) ||
            propertySpec.getName().equals(DeviceMessageConstants.StartOfDSTAttributeName) ||
            propertySpec.getName().equals(DeviceMessageConstants.EndOfDSTAttributeName)) {
            return String.valueOf(((Date) messageAttribute).getTime());
        } else if (propertySpec.getName().equals(DeviceMessageConstants.newPasswordAttributeName)) {
            return keyAccessorTypeExtractor.passiveValueContent((KeyAccessorType) messageAttribute);
        } else if (propertySpec.getName().equals(DeviceMessageConstants.newAuthenticationKeyAttributeName)) {
            return keyAccessorTypeExtractor.passiveValueContent((KeyAccessorType) messageAttribute);
        } else if (propertySpec.getName().equals(DeviceMessageConstants.newEncryptionKeyAttributeName)) {
            return keyAccessorTypeExtractor.passiveValueContent((KeyAccessorType) messageAttribute);
        } else {
            return messageAttribute.toString();
        }
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return Optional.empty();
    }

    public PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    public NlsService getNlsService() {
        return nlsService;
    }

    public Converter getConverter() {
        return converter;
    }
}