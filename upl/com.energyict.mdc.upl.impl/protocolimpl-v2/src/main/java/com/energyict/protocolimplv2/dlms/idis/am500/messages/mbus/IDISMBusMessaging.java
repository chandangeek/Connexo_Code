package com.energyict.protocolimplv2.dlms.idis.am500.messages.mbus;

import com.energyict.cbo.Password;
import com.energyict.cbo.TimeDuration;
import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.CollectedMessageList;
import com.energyict.mdc.protocol.tasks.support.DeviceMessageSupport;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractDlmsMessaging;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 6/01/2015 - 15:34
 */
public class IDISMBusMessaging extends AbstractDlmsMessaging implements DeviceMessageSupport {

    private final static List<DeviceMessageSpec> supportedMessages;

    static {
        supportedMessages = new ArrayList<>();

        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_OPEN);
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_CLOSE);
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE);
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE);

        supportedMessages.add(MBusSetupDeviceMessage.Decommission);
        supportedMessages.add(MBusSetupDeviceMessage.SetEncryptionKeys);
        supportedMessages.add(MBusSetupDeviceMessage.WriteCaptureDefinitionForAllInstances);
        supportedMessages.add(MBusSetupDeviceMessage.WriteMBusCapturePeriod);
    }

    public IDISMBusMessaging(AbstractDlmsProtocol protocol) {
        super(protocol);
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return supportedMessages;
    }

    /**
     * Not supported here, message(s) will be executed in the e-meter protocol
     */
    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "executePendingMessages");
    }

    /**
     * Not supported here, message(s) will be executed in the e-meter protocol
     */
    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> offlineDeviceMessages) {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "updateSentMessages");
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(contactorActivationDateAttributeName)) {
            return String.valueOf(((Date) messageAttribute).getTime());     //Epoch
        } else if (propertySpec.getName().equals(capturePeriodAttributeName)) {
            return String.valueOf(((TimeDuration) messageAttribute).getSeconds());
        } else if (propertySpec.getName().equals(openKeyAttributeName) || propertySpec.getName().equals(transferKeyAttributeName)) {
            return ((Password) messageAttribute).getValue();
        }
        return messageAttribute.toString();
    }
}