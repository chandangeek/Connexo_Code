package com.energyict.protocolimplv2.messages.convertor;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.HexString;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.protocol.api.UserFile;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.protocolimpl.generic.messages.GenericMessaging;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.AdvancedTagMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleValueMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.FirmwareUdateWithUserFileMessageEntry;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.*;

/**
 * Represents a MessageConverter for the Prime meter protocols
 *
 * @author khe
 * @since 25/10/13 - 10:46
 */
public class PrimeMeterMessageConverter extends AbstractMessageConverter {

    public PrimeMeterMessageConverter() {
        super();
    }

    @Override
    protected Map<DeviceMessageId, MessageEntryCreator> getRegistry() {
        Map<DeviceMessageId, MessageEntryCreator> registry = new HashMap<>();
        registry.put(DeviceMessageId.CONTACTOR_CLOSE, new SimpleTagMessageEntry("ConnectMain"));
        registry.put(DeviceMessageId.CONTACTOR_OPEN, new SimpleTagMessageEntry("DisconnectMain"));
        registry.put(DeviceMessageId.CONTACTOR_CLOSE_RELAY, new AdvancedTagMessageEntry("ConnectRelay"));
        registry.put(DeviceMessageId.CONTACTOR_OPEN_RELAY, new AdvancedTagMessageEntry("DisconnectRelay"));
        registry.put(DeviceMessageId.CLOCK_SET_TIMEZONE_OFFSET, new MultipleAttributeMessageEntry("DisconnectRelay", "GMT offset (in hours)"));

        registry.put(DeviceMessageId.ACTIVITY_CALENDAR_WRITE_CONTRACTS_FROM_XML_USERFILE, new SimpleValueMessageEntry("WriteContracts"));

        registry.put(DeviceMessageId.LOAD_BALANCING_WRITE_CONTROL_THRESHOLDS, new MultipleAttributeMessageEntry("WriteControlThresholds", "Threshold 1 (unit W)", "Threshold 2 (unit W)", "Threshold 3 (unit W)", "Threshold 4 (unit W)", "Threshold 5 (unit W)", "Threshold 6 (unit W)", "ActivationDate"));
        registry.put(DeviceMessageId.LOAD_BALANCING_SET_DEMAND_CLOSE_TO_CONTRACT_POWER_THRESHOLD, new MultipleAttributeMessageEntry("SetDemandCloseToContractPowerThreshold", "Threshold (%)"));

        registry.put(DeviceMessageId.POWER_CONFIGURATION_SET_REFERENCE_VOLTAGE, new MultipleAttributeMessageEntry("SetReferenceVoltage", "Reference voltage (V)"));
        registry.put(DeviceMessageId.POWER_CONFIGURATION_SET_VOLTAGE_SAG_TIME_THRESHOLD, new MultipleAttributeMessageEntry("SetVoltageSagTimeThreshold", "Time threshold (seconds)"));
        registry.put(DeviceMessageId.POWER_CONFIGURATION_SET_VOLTAGE_SWELL_TIME_THRESHOLD, new MultipleAttributeMessageEntry("SetVoltageSwellTimeThreshold", "Time threshold (seconds)"));
        registry.put(DeviceMessageId.POWER_CONFIGURATION_SET_VOLTAGE_SAG_THRESHOLD, new MultipleAttributeMessageEntry("SetVoltageSagThreshold", "Threshold (%)"));
        registry.put(DeviceMessageId.POWER_CONFIGURATION_SET_VOLTAGE_SWELL_THRESHOLD, new MultipleAttributeMessageEntry("SetVoltageSwellThreshold", "Threshold (%)"));
        registry.put(DeviceMessageId.POWER_CONFIGURATION_SET_LONG_POWER_FAILURE_TIME_THRESHOLD, new MultipleAttributeMessageEntry("SetLongPowerFailureTimeThreshold", "Time threshold (seconds)"));
        registry.put(DeviceMessageId.POWER_CONFIGURATION_SET_LONG_POWER_FAILURE_THRESHOLD, new MultipleAttributeMessageEntry("SetLongPowerFailureThreshold", "Threshold (%)"));

        registry.put(DeviceMessageId.PLC_CONFIGURATION_SET_MULTICAST_ADDRESSES, new MultipleAttributeMessageEntry("SetMulticastAddresses", "Address 1", "Address 2", "Address 3"));
        registry.put(DeviceMessageId.SECURITY_CHANGE_CLIENT_PASSWORDS, new MultipleAttributeMessageEntry("ChangePasswords", "reading", "management", "firmware"));

        registry.put(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE, new FirmwareUdateWithUserFileMessageEntry(firmwareUpdateFileAttributeName));
        return registry;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(contractsXmlUserFileAttributeName)) {
            return new String(((UserFile) messageAttribute).loadFileInByteArray());   //String = XML content in the userfile
        } else if (propertySpec.getName().equals(activationDatedAttributeName)) {
            return String.valueOf(((Date) messageAttribute).getTime());
        } else if (propertySpec.getName().equals(MulticastAddress1AttributeName)
                || propertySpec.getName().equals(MulticastAddress2AttributeName)
                || propertySpec.getName().equals(MulticastAddress3AttributeName)) {
            return ((HexString) messageAttribute).getContent();
        } else if (propertySpec.getName().equals(firmwareUpdateFileAttributeName)) {
            FirmwareVersion firmwareVersion = ((FirmwareVersion) messageAttribute);
            return GenericMessaging.zipAndB64EncodeContent(firmwareVersion.getFirmwareFile());  //Bytes of the firmwareFile as string
        }
        return messageAttribute.toString();
    }

}