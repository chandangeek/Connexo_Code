package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.common.HexString;
import com.energyict.mdc.protocol.api.UserFile;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.impl.device.messages.ActivityCalendarDeviceMessage;
import com.energyict.mdc.protocol.api.impl.device.messages.ClockDeviceMessage;
import com.energyict.mdc.protocol.api.impl.device.messages.ContactorDeviceMessage;
import com.energyict.mdc.protocol.api.impl.device.messages.FirmwareDeviceMessage;
import com.energyict.mdc.protocol.api.impl.device.messages.LoadBalanceDeviceMessage;
import com.energyict.mdc.protocol.api.impl.device.messages.PLCConfigurationDeviceMessage;
import com.energyict.mdc.protocol.api.impl.device.messages.PowerConfigurationDeviceMessage;
import com.energyict.mdc.protocol.api.impl.device.messages.SecurityMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.AdvancedTagMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleValueMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.FirmwareUdateWithUserFileMessageEntry;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.energyict.mdc.protocol.api.impl.device.messages.DeviceMessageConstants.MulticastAddress2AttributeName;
import static com.energyict.mdc.protocol.api.impl.device.messages.DeviceMessageConstants.MulticastAddress3AttributeName;
import static com.energyict.mdc.protocol.api.impl.device.messages.DeviceMessageConstants.activationDatedAttributeName;
import static com.energyict.mdc.protocol.api.impl.device.messages.DeviceMessageConstants.contractsXmlUserFileAttributeName;
import static com.energyict.mdc.protocol.api.impl.device.messages.DeviceMessageConstants.firmwareUpdateUserFileAttributeName;

/**
 * Represents a MessageConverter for the Prime meter protocols
 *
 * @author khe
 * @since 25/10/13 - 10:46
 */
public class PrimeMeterMessageConverter extends AbstractMessageConverter {

    /**
     * Represents a mapping between {@link DeviceMessageSpec}s
     * and the corresponding {@link MessageEntryCreator}
     */
    private static Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>();

    static {
        registry.put(ContactorDeviceMessage.CONTACTOR_CLOSE, new SimpleTagMessageEntry("ConnectMain"));
        registry.put(ContactorDeviceMessage.CONTACTOR_OPEN, new SimpleTagMessageEntry("DisconnectMain"));
        registry.put(ContactorDeviceMessage.CLOSE_RELAY, new AdvancedTagMessageEntry("ConnectRelay"));
        registry.put(ContactorDeviceMessage.OPEN_RELAY, new AdvancedTagMessageEntry("DisconnectRelay"));
        registry.put(ClockDeviceMessage.SET_TIMEZONE_OFFSET, new MultipleAttributeMessageEntry("DisconnectRelay", "GMT offset (in hours)"));

        registry.put(ActivityCalendarDeviceMessage.WRITE_CONTRACTS_FROM_XML_USERFILE, new SimpleValueMessageEntry("WriteContracts"));

        registry.put(LoadBalanceDeviceMessage.WriteControlThresholds, new MultipleAttributeMessageEntry("WriteControlThresholds", "Threshold 1 (unit W)", "Threshold 2 (unit W)", "Threshold 3 (unit W)", "Threshold 4 (unit W)", "Threshold 5 (unit W)", "Threshold 6 (unit W)", "ActivationDate"));
        registry.put(LoadBalanceDeviceMessage.SetDemandCloseToContractPowerThreshold, new MultipleAttributeMessageEntry("SetDemandCloseToContractPowerThreshold", "Threshold (%)"));

        registry.put(PowerConfigurationDeviceMessage.SetReferenceVoltage, new MultipleAttributeMessageEntry("SetReferenceVoltage", "Reference voltage (V)"));
        registry.put(PowerConfigurationDeviceMessage.SetVoltageSagTimeThreshold, new MultipleAttributeMessageEntry("SetVoltageSagTimeThreshold", "Time threshold (seconds)"));
        registry.put(PowerConfigurationDeviceMessage.SetVoltageSwellTimeThreshold, new MultipleAttributeMessageEntry("SetVoltageSwellTimeThreshold", "Time threshold (seconds)"));
        registry.put(PowerConfigurationDeviceMessage.SetVoltageSagThreshold, new MultipleAttributeMessageEntry("SetVoltageSagThreshold", "Threshold (%)"));
        registry.put(PowerConfigurationDeviceMessage.SetVoltageSwellThreshold, new MultipleAttributeMessageEntry("SetVoltageSwellThreshold", "Threshold (%)"));
        registry.put(PowerConfigurationDeviceMessage.SetLongPowerFailureTimeThreshold, new MultipleAttributeMessageEntry("SetLongPowerFailureTimeThreshold", "Time threshold (seconds)"));
        registry.put(PowerConfigurationDeviceMessage.SetLongPowerFailureThreshold, new MultipleAttributeMessageEntry("SetLongPowerFailureThreshold", "Threshold (%)"));

        registry.put(PLCConfigurationDeviceMessage.SetMulticastAddresses, new MultipleAttributeMessageEntry("SetMulticastAddresses", "Address 1", "Address 2", "Address 3"));
        registry.put(SecurityMessage.CHANGE_CLIENT_PASSWORDS, new MultipleAttributeMessageEntry("ChangePasswords", "reading", "management", "firmware"));

        registry.put(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE, new FirmwareUdateWithUserFileMessageEntry(firmwareUpdateUserFileAttributeName));
    }

    public PrimeMeterMessageConverter() {
        super();
    }

    @Override
    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return registry;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(contractsXmlUserFileAttributeName)) {
            return new String(((UserFile) messageAttribute).loadFileInByteArray());   //String = XML content in the userfile
        } else if (propertySpec.getName().equals(activationDatedAttributeName)) {
            return String.valueOf(((Date) messageAttribute).getTime());
        } else if (propertySpec.getName().equals(MulticastAddress2AttributeName)
                || propertySpec.getName().equals(MulticastAddress2AttributeName)
                || propertySpec.getName().equals(MulticastAddress3AttributeName)) {
            return ((HexString) messageAttribute).getContent();
        } else if (propertySpec.getName().equals(firmwareUpdateUserFileAttributeName)) {
            return new String(((UserFile) messageAttribute).loadFileInByteArray());
        }
        return messageAttribute.toString();
    }
}