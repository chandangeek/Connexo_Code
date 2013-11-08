package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cpo.PropertySpec;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.OneTagMessageEntry;
import com.energyict.protocolimplv2.messages.enums.DlmsAuthenticationLevelMessageValues;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.authenticationLevelAttributeName;

/**
 * Represents a MessageConverter for the legacy DSMR4.0 L&G DE350 protocol.
 *
 * @author sva
 * @since 30/10/13 - 14:00
 */
public class Dsmr40MessageConverter extends Dsmr23MessageConverter {

    static {
        // Restore factory settings
        registry.put(DeviceActionMessage.RESTORE_FACTORY_SETTINGS, new OneTagMessageEntry("Restore_Factory_Settings"));

        // Change administrative status
        registry.put(ConfigurationChangeDeviceMessage.ChangeAdministrativeStatus, new MultipleAttributeMessageEntry("Change_Administrative_Status", "Status"));

        // Authentication and encryption - remove the DSMR2.3 message & replace by 4 new DSMR4.0 messages
        registry.remove(SecurityMessage.CHANGE_DLMS_AUTHENTICATION_LEVEL);
        registry.put(SecurityMessage.DISABLE_DLMS_AUTHENTICATION_LEVEL_P0, new MultipleAttributeMessageEntry("Disable_authentication_level_P0", "AuthenticationLevel"));
        registry.put(SecurityMessage.DISABLE_DLMS_AUTHENTICATION_LEVEL_P1, new MultipleAttributeMessageEntry("Disable_authentication_level_P1", "AuthenticationLevel"));
        registry.put(SecurityMessage.ENABLE_DLMS_AUTHENTICATION_LEVEL_P0, new MultipleAttributeMessageEntry("Enable_authentication_level_P0", "AuthenticationLevel"));
        registry.put(SecurityMessage.ENABLE_DLMS_AUTHENTICATION_LEVEL_P1, new MultipleAttributeMessageEntry("Enable_authentication_level_P1", "AuthenticationLevel"));
    }

    /**
     * Default constructor for at-runtime instantiation
     */
    public Dsmr40MessageConverter() {
        super();
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
