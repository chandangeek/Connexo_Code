package com.energyict.protocolimplv2.elster.garnet;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessage;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.CollectedMessageList;
import com.energyict.mdc.protocol.tasks.support.DeviceMessageSupport;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocol.exceptions.CodingException;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sva
 * @since 27/06/2014 - 13:26
 */
public class EMeterMessaging implements DeviceMessageSupport {

    private final static List<DeviceMessageSpec> supportedMessages;

    private final A100C deviceProtocol;

    static {
        supportedMessages = new ArrayList<>();

        // contactor related
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_OPEN);
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_CLOSE);
    }


    public EMeterMessaging(A100C deviceProtocol) {
        this.deviceProtocol = deviceProtocol;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return supportedMessages;
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        return messageAttribute.toString();
    }

    @Override
    public String prepareMessageContext(OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return "";
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