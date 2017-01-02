package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.LoadProfileExtractor;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.messages.legacy.NumberLookupExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.WebRTUFirmwareUpgradeWithUserFileActivationDateMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.OneTagMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagMessageEntry;
import com.energyict.protocolimplv2.messages.enums.DlmsAuthenticationLevelMessageValues;

import java.util.HashMap;
import java.util.Map;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.authenticationLevelAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateImageIdentifierAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateUserFileAttributeName;

/**
 * Represents a MessageConverter for the legacy DSMR4.0 L&G DE350 protocol.
 *
 * @author sva
 * @since 30/10/13 - 14:00
 */
public class Dsmr40MessageConverter extends Dsmr23MessageConverter {

    public Dsmr40MessageConverter(Messaging messagingProtocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, LoadProfileExtractor loadProfileExtractor, NumberLookupExtractor numberLookupExtractor, DeviceMessageFileExtractor messageFileExtractor, TariffCalendarExtractor calendarExtractor) {
        super(messagingProtocol, propertySpecService, nlsService, converter, loadProfileExtractor, numberLookupExtractor, messageFileExtractor, calendarExtractor);
    }

    @Override
    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>(super.getRegistry());
        // Restore factory settings
        registry.put(messageSpec(DeviceActionMessage.RESTORE_FACTORY_SETTINGS), new OneTagMessageEntry("Restore_Factory_Settings"));

        // Change administrative status
        registry.put(messageSpec(ConfigurationChangeDeviceMessage.ChangeAdministrativeStatus), new MultipleAttributeMessageEntry("Change_Administrative_Status", "Status"));

        // Authentication and encryption - remove the DSMR2.3 message & replace by 4 new DSMR4.0 messages
        registry.remove(messageSpec(SecurityMessage.CHANGE_DLMS_AUTHENTICATION_LEVEL));
        registry.put(messageSpec(SecurityMessage.DISABLE_DLMS_AUTHENTICATION_LEVEL_P0), new MultipleAttributeMessageEntry("Disable_authentication_level_P0", "AuthenticationLevel"));
        registry.put(messageSpec(SecurityMessage.DISABLE_DLMS_AUTHENTICATION_LEVEL_P3), new MultipleAttributeMessageEntry("Disable_authentication_level_P3", "AuthenticationLevel"));
        registry.put(messageSpec(SecurityMessage.ENABLE_DLMS_AUTHENTICATION_LEVEL_P0), new MultipleAttributeMessageEntry("Enable_authentication_level_P0", "AuthenticationLevel"));
        registry.put(messageSpec(SecurityMessage.ENABLE_DLMS_AUTHENTICATION_LEVEL_P3), new MultipleAttributeMessageEntry("Enable_authentication_level_P3", "AuthenticationLevel"));

        registry.put(messageSpec(ConfigurationChangeDeviceMessage.ENABLE_DISCOVERY_ON_POWER_UP), new SimpleTagMessageEntry(RtuMessageConstant.ENABLE_DISCOVERY_ON_POWER_UP));
        registry.put(messageSpec(ConfigurationChangeDeviceMessage.DISABLE_DISCOVERY_ON_POWER_UP), new SimpleTagMessageEntry(RtuMessageConstant.DISABLE_DISCOVERY_ON_POWER_UP));

        // Firmware upgrade
        registry.put(messageSpec(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_IMAGE_IDENTIFIER), new WebRTUFirmwareUpgradeWithUserFileActivationDateMessageEntry(firmwareUpdateUserFileAttributeName, null, firmwareUpdateImageIdentifierAttributeName));
        return registry;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        switch (propertySpec.getName()) {
            case authenticationLevelAttributeName:
                return String.valueOf(DlmsAuthenticationLevelMessageValues.getValueFor(messageAttribute.toString()));
            default:
                return super.format(propertySpec, messageAttribute);
        }
    }

}