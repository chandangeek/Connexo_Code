package com.energyict.protocolimplv2.eict.webrtuz3.messages.emeter;

import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;

import com.energyict.protocol.exceptions.CodingException;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.eict.webrtuz3.messages.WebRTUZ3MessageExecutor;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractDlmsMessaging;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contactorActivationDateAttributeName;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 24/04/2015 - 13:17
 */
public class WebRTUZ3EMeterMessaging extends AbstractDlmsMessaging implements DeviceMessageSupport {

    private final Converter converter;
    private final NlsService nlsService;
    private final PropertySpecService propertySpecService;

    protected WebRTUZ3MessageExecutor messageExecutor;

    public WebRTUZ3EMeterMessaging(AbstractDlmsProtocol protocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        super(protocol);
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
        this.converter = converter;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return Arrays.asList(
                    ContactorDeviceMessage.CONTACTOR_OPEN.get(this.propertySpecService, this.nlsService, this.converter),
                    ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE.get(this.propertySpecService, this.nlsService, this.converter),
                    ContactorDeviceMessage.CONTACTOR_CLOSE.get(this.propertySpecService, this.nlsService, this.converter),
                    ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE.get(this.propertySpecService, this.nlsService, this.converter),
                    ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE.get(this.propertySpecService, this.nlsService, this.converter));
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        throw CodingException.unsupportedMethod(this.getClass(), "executePendingMessages");
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> offlineDeviceMessages) {
        throw CodingException.unsupportedMethod(this.getClass(), "updateSentMessages");
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, com.energyict.mdc.upl.properties.PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(contactorActivationDateAttributeName)) {
            return String.valueOf(((Date) messageAttribute).getTime());
        }
        return messageAttribute.toString();
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return Optional.empty();
    }

}