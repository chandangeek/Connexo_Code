package com.energyict.protocolimplv2.elster.garnet;

import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;

import com.energyict.protocol.exceptions.CodingException;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author sva
 * @since 27/06/2014 - 13:26
 */
public class EMeterMessaging implements DeviceMessageSupport {

    private final A100C deviceProtocol;
    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;
    private final Converter converter;

    public EMeterMessaging(A100C deviceProtocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        this.deviceProtocol = deviceProtocol;
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
        this.converter = converter;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return Arrays.asList(
                ContactorDeviceMessage.CONTACTOR_OPEN.get(this.propertySpecService, this.nlsService, this.converter),
                ContactorDeviceMessage.CONTACTOR_CLOSE.get(this.propertySpecService, this.nlsService, this.converter));
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        return messageAttribute.toString();
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return Optional.empty();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        throw CodingException.unsupportedMethod(this.getClass(), "executePendingMessages");
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        throw CodingException.unsupportedMethod(this.getClass(), "updateSentMessages");
    }

    public A100C getDeviceProtocol() {
        return deviceProtocol;
    }
}