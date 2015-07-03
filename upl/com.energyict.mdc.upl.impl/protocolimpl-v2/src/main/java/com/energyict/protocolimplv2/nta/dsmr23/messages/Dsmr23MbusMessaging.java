package com.energyict.protocolimplv2.nta.dsmr23.messages;

import com.energyict.cbo.Password;
import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.CollectedMessageList;
import com.energyict.mdc.protocol.tasks.support.DeviceMessageSupport;
import com.energyict.mdw.core.LoadProfile;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.LoadProfileMessage;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.utils.LoadProfileMessageUtils;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractNtaMbusDevice;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractDlmsMessaging;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Implementation of {@link DeviceMessageSupport} for the DSMR 2.3 Mbus slave device, that:
 * - Formats the device message attributes from objects to proper string values
 * - Executes a given message
 * - Has a list of all supported device message specs
 * <p/>
 *
 * @author sva
 * @since 29/11/13 - 14:17
 */
public class Dsmr23MbusMessaging extends AbstractDlmsMessaging implements DeviceMessageSupport {

    private final static List<DeviceMessageSpec> supportedMessages;

    static {
        supportedMessages = new ArrayList<>();

        // contactor related
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_OPEN);
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE);
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_CLOSE);
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE);
        supportedMessages.add(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE);

        // Mbus setup
        supportedMessages.add(MBusSetupDeviceMessage.Decommission);
        supportedMessages.add(MBusSetupDeviceMessage.SetEncryptionKeys);
        supportedMessages.add(MBusSetupDeviceMessage.UseCorrectedValues);
        supportedMessages.add(MBusSetupDeviceMessage.UseUncorrectedValues);

        // LoadProfiles
        supportedMessages.add(LoadProfileMessage.PARTIAL_LOAD_PROFILE_REQUEST);
        supportedMessages.add(LoadProfileMessage.LOAD_PROFILE_REGISTER_REQUEST);
    }

    public Dsmr23MbusMessaging(AbstractNtaMbusDevice mbusProtocol) {
        super(mbusProtocol.getMeterProtocol());
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return supportedMessages;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.loadProfileAttributeName:
                return LoadProfileMessageUtils.formatLoadProfile((LoadProfile) messageAttribute);
            case DeviceMessageConstants.openKeyAttributeName:
            case DeviceMessageConstants.transferKeyAttributeName:
                return ((Password) messageAttribute).getValue();
            case DeviceMessageConstants.fromDateAttributeName:
            case DeviceMessageConstants.toDateAttributeName:
                return String.valueOf(((Date) messageAttribute).getTime());
            case DeviceMessageConstants.contactorActivationDateAttributeName:
                return String.valueOf(((Date) messageAttribute).getTime());  //Epoch (millis)
            default:
                return messageAttribute.toString();  //Used for String and BigDecimal attributes
        }
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "updateSentMessages");
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "executePendingMessages");
    }
}