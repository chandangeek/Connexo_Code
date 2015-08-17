package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cbo.Password;
import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ApnCredentialsMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.OneTagMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.FirmwareUdateWithUserFileMessageEntry;

import java.util.HashMap;
import java.util.Map;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;

/**
 * @author sva
 * @since 13/08/2015 - 16:04
 */
public class EK280MessageConverter extends AbstractMessageConverter {

    /**
     * Represents a mapping between {@link com.energyict.mdc.messages.DeviceMessageSpec deviceMessageSpecs}
     * and the corresponding {@link MessageEntryCreator}
     */
    private static Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>();

    static {
        // Network and connectivity
        registry.put(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS, new ApnCredentialsMessageEntry(apnAttributeName, usernameAttributeName, passwordAttributeName));

        // Configuration change
        registry.put(ConfigurationChangeDeviceMessage.WriteNewPDRNumber, new MultipleAttributeMessageEntry("WritePDR", "PdrToWrite"));

        // Activity calendar
        registry.put(ActivityCalendarDeviceMessage.CLEAR_AND_DISABLE_PASSIVE_TARIFF, new OneTagMessageEntry("ClearPassiveTariff"));

        // Security messages
        registry.put(SecurityMessage.CHANGE_SECURITY_KEYS, new MultipleAttributeMessageEntry("ChangeKeys", "ClientId", "WrapperKey", "NewAuthenticationKey", "NewEncryptionKey"));

        // Firmware upgrade
        registry.put(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE, new FirmwareUdateWithUserFileMessageEntry(firmwareUpdateUserFileAttributeName));
    }

    /**
     * Default constructor for at-runtime instantiation
     */
    public EK280MessageConverter() {
        super();
    }

    @Override
    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return registry;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(passwordAttributeName) ||
                propertySpec.getName().equals(masterKey) ||
                propertySpec.getName().equals(newAuthenticationKeyAttributeName) ||
                propertySpec.getName().equals(newEncryptionKeyAttributeName)) {
            return ((Password) messageAttribute).getValue();
        } else {
            return messageAttribute.toString();
        }
    }
}
