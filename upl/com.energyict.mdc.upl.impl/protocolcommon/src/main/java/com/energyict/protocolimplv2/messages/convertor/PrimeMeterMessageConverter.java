package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.HexString;
import com.energyict.mdc.upl.properties.Password;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.ClockDeviceMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadBalanceDeviceMessage;
import com.energyict.protocolimplv2.messages.PLCConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.PowerConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.AdvancedTagMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleValueMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.FirmwareUdateWithUserFileMessageEntry;
import com.google.common.collect.ImmutableMap;

import java.util.Date;
import java.util.Map;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.MulticastAddress1AttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.MulticastAddress2AttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.MulticastAddress3AttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activationDatedAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contractsXmlUserFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateUserFileAttributeName;

/**
 * Represents a MessageConverter for the Prime meter protocols
 *
 * @author khe
 * @since 25/10/13 - 10:46
 */
public class PrimeMeterMessageConverter extends AbstractMessageConverter {

    private final Extractor extractor;

    public PrimeMeterMessageConverter(Messaging messagingProtocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, Extractor extractor) {
        super(messagingProtocol, propertySpecService, nlsService, converter);
        this.extractor = extractor;
    }

    @Override
    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return ImmutableMap
                .<DeviceMessageSpec, MessageEntryCreator>builder()
                .put(messageSpec(ContactorDeviceMessage.CONTACTOR_CLOSE), new SimpleTagMessageEntry("ConnectMain"))
                .put(messageSpec(ContactorDeviceMessage.CONTACTOR_OPEN), new SimpleTagMessageEntry("DisconnectMain"))
                .put(messageSpec(ContactorDeviceMessage.CLOSE_RELAY), new AdvancedTagMessageEntry("ConnectRelay"))
                .put(messageSpec(ContactorDeviceMessage.OPEN_RELAY), new AdvancedTagMessageEntry("DisconnectRelay"))
                .put(messageSpec(ClockDeviceMessage.SET_TIMEZONE), new MultipleAttributeMessageEntry("DisconnectRelay", "GMT offset (in hours)"))

                .put(messageSpec(ActivityCalendarDeviceMessage.WRITE_CONTRACTS_FROM_XML_USERFILE), new SimpleValueMessageEntry("WriteContracts"))

                .put(messageSpec(LoadBalanceDeviceMessage.WriteControlThresholds), new MultipleAttributeMessageEntry("WriteControlThresholds", "Threshold 1 (unit W)", "Threshold 2 (unit W)", "Threshold 3 (unit W)", "Threshold 4 (unit W)", "Threshold 5 (unit W)", "Threshold 6 (unit W)", "ActivationDate"))
                .put(messageSpec(LoadBalanceDeviceMessage.SetDemandCloseToContractPowerThreshold), new MultipleAttributeMessageEntry("SetDemandCloseToContractPowerThreshold", "Threshold (%)"))

                .put(messageSpec(PowerConfigurationDeviceMessage.SetReferenceVoltage), new MultipleAttributeMessageEntry("SetReferenceVoltage", "Reference voltage (V)"))
                .put(messageSpec(PowerConfigurationDeviceMessage.SetVoltageSagTimeThreshold), new MultipleAttributeMessageEntry("SetVoltageSagTimeThreshold", "Time threshold (seconds)"))
                .put(messageSpec(PowerConfigurationDeviceMessage.SetVoltageSwellTimeThreshold), new MultipleAttributeMessageEntry("SetVoltageSwellTimeThreshold", "Time threshold (seconds)"))
                .put(messageSpec(PowerConfigurationDeviceMessage.SetVoltageSagThreshold), new MultipleAttributeMessageEntry("SetVoltageSagThreshold", "Threshold (%)"))
                .put(messageSpec(PowerConfigurationDeviceMessage.SetVoltageSwellThreshold), new MultipleAttributeMessageEntry("SetVoltageSwellThreshold", "Threshold (%)"))
                .put(messageSpec(PowerConfigurationDeviceMessage.SetLongPowerFailureTimeThreshold), new MultipleAttributeMessageEntry("SetLongPowerFailureTimeThreshold", "Time threshold (seconds)"))
                .put(messageSpec(PowerConfigurationDeviceMessage.SetLongPowerFailureThreshold), new MultipleAttributeMessageEntry("SetLongPowerFailureThreshold", "Threshold (%)"))

                .put(messageSpec(PLCConfigurationDeviceMessage.SetMulticastAddresses), new MultipleAttributeMessageEntry("SetMulticastAddresses", "Address 1", "Address 2", "Address 3"))
                .put(messageSpec(SecurityMessage.CHANGE_CLIENT_PASSWORDS), new MultipleAttributeMessageEntry("ChangePasswords", "reading", "management", "firmware"))

                .put(messageSpec(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE), new FirmwareUdateWithUserFileMessageEntry(firmwareUpdateUserFileAttributeName))
                .build();
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(contractsXmlUserFileAttributeName)) {
            return this.extractor.contents((DeviceMessageFile) messageAttribute);   //String = XML content in the userfile
        } else if (propertySpec.getName().equals(activationDatedAttributeName)) {
            return String.valueOf(((Date) messageAttribute).getTime());
        } else if (propertySpec.getName().equals(MulticastAddress1AttributeName)
                || propertySpec.getName().equals(MulticastAddress2AttributeName)
                || propertySpec.getName().equals(MulticastAddress3AttributeName)) {
            return ((HexString) messageAttribute).getContent();
        } else if (propertySpec.getName().equals(firmwareUpdateUserFileAttributeName)) {
            return this.extractor.contents((DeviceMessageFile) messageAttribute);
        } else if (propertySpec.getName().equals(DeviceMessageConstants.newReadingClientPasswordAttributeName)
                || propertySpec.getName().equals(DeviceMessageConstants.newManagementClientPasswordAttributeName)
                || propertySpec.getName().equals(DeviceMessageConstants.newFirmwareClientPasswordAttributeName)) {
            return ((Password) messageAttribute).getValue();
        }
        return messageAttribute.toString();
    }
}