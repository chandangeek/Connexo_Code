package com.energyict.protocolimplv2.dlms.idis.am500.messages.mbus;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.Password;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.tasks.support.DeviceMessageSupport;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractDlmsMessaging;
import com.energyict.protocols.exception.UnsupportedMethodException;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Copyrights EnergyICT
 * Date: 29.09.15
 * Time: 14:43
 */
public class IDISMBusMessaging extends AbstractDlmsMessaging implements DeviceMessageSupport {

    private final static Set<DeviceMessageId> supportedMessages;

    static {
        supportedMessages = new HashSet<>();

        supportedMessages.add(DeviceMessageId.CONTACTOR_OPEN);
        supportedMessages.add(DeviceMessageId.CONTACTOR_CLOSE);
        supportedMessages.add(DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE);
        supportedMessages.add(DeviceMessageId.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE);

        supportedMessages.add(DeviceMessageId.MBUS_SETUP_DECOMMISSION);
        supportedMessages.add(DeviceMessageId.MBUS_SETUP_SET_ENCRYPTION_KEYS);
        supportedMessages.add(DeviceMessageId.MBUS_SETUP_WRITE_CAPTURE_DEFINITION_FOR_ALL_INSTANCES);
        supportedMessages.add(DeviceMessageId.MBUS_SETUP_WRITE_CAPTURE_PERIOD);
    }

    public IDISMBusMessaging(AbstractDlmsProtocol protocol) {
        super(protocol);
    }

    @Override
    public Set<DeviceMessageId> getSupportedMessages() {
        return supportedMessages;
    }

    /**
     * Not supported here, message(s) will be executed in the e-meter protocol
     */
    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        throw new UnsupportedMethodException(this.getClass(), "executePendingMessages");
    }

    /**
     * Not supported here, message(s) will be executed in the e-meter protocol
     */
    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> offlineDeviceMessages) {
        throw new UnsupportedMethodException(this.getClass(), "updateSentMessages");
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(DeviceMessageConstants.contactorActivationDateAttributeName)) {
            return String.valueOf(((Date) messageAttribute).getTime());     //Epoch
        } else if (propertySpec.getName().equals(DeviceMessageConstants.capturePeriodAttributeName)) {
            return String.valueOf(((TimeDuration) messageAttribute).getSeconds());
        } else if (propertySpec.getName().equals(DeviceMessageConstants.openKeyAttributeName) || propertySpec.getName().equals(DeviceMessageConstants.transferKeyAttributeName)) {
            return ((Password) messageAttribute).getValue();
        }
        return messageAttribute.toString();
    }
}