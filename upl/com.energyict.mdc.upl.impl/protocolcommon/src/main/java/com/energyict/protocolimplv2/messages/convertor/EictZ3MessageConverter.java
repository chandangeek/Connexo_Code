package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ConnectControlModeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ConnectLoadWithActivationDateMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.DisconnectLoadWithActivationDateMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.SetMBusEncryptionKeysMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.OneTagMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.FirmwareUdateWithUserFileMessageEntry;
import com.google.common.collect.ImmutableMap;

import java.util.Date;
import java.util.Map;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contactorActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contactorModeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.openKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.transferKeyAttributeName;

/**
 * Represents a MessageConverter for the legacy EictZ3 protocol
 * <p/>
 * Copyrights EnergyICT
 * Date: 8/03/13
 * Time: 16:26
 */
public class EictZ3MessageConverter extends AbstractMessageConverter {

    public EictZ3MessageConverter(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        super(propertySpecService, nlsService, converter);
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(contactorActivationDateAttributeName)) {
            return dateTimeFormat.format((Date) messageAttribute);
        } else if (propertySpec.getName().equals(contactorModeAttributeName)) {
            return messageAttribute.toString();
        } else if (propertySpec.getName().equals(openKeyAttributeName) || propertySpec.getName().equals(transferKeyAttributeName)) {
            return messageAttribute.toString(); // Reference<KeyAccessorType> is already resolved to actual key by framework before passing on to protocols
        } else if (propertySpec.getName().equals(firmwareUpdateFileAttributeName)) {
            return messageAttribute.toString();     //This is the path of the temp file representing the FirmwareVersion
        }
        return EMPTY_FORMAT;
    }

    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return ImmutableMap
                .<DeviceMessageSpec, MessageEntryCreator>builder()
                // contactor related
                .put(messageSpec(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE), new DisconnectLoadWithActivationDateMessageEntry(contactorActivationDateAttributeName))
                .put(messageSpec(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE), new ConnectLoadWithActivationDateMessageEntry(contactorActivationDateAttributeName))
                .put(messageSpec(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE), new ConnectControlModeMessageEntry(contactorModeAttributeName))

                // mbus related
                .put(messageSpec(MBusSetupDeviceMessage.Decommission), new OneTagMessageEntry(RtuMessageConstant.MBUS_DECOMMISSION))
                .put(messageSpec(MBusSetupDeviceMessage.SetEncryptionKeys), new SetMBusEncryptionKeysMessageEntry(openKeyAttributeName, transferKeyAttributeName))

                .put(messageSpec(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE), new FirmwareUdateWithUserFileMessageEntry(firmwareUpdateFileAttributeName))
                .build();
    }
}