package com.energyict.protocolimplv2.messages.convertor;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.HexString;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.protocolimpl.generic.messages.GenericMessaging;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ConnectControlModeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ConnectLoadWithActivationDateMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.DisconnectLoadWithActivationDateMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.SetMBusEncryptionKeysMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.OneTagMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.FirmwareUdateWithUserFileMessageEntry;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.*;

/**
 * Represents a MessageConverter for the legacy EictZ3 protocol
 * <p/>
 * Copyrights EnergyICT
 * Date: 8/03/13
 * Time: 16:26
 */
public class EictZ3MessageConverter extends AbstractMessageConverter {


    /**
     * Default constructor for at-runtime instantiation
     */
    public EictZ3MessageConverter() {
        super();
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(contactorActivationDateAttributeName)) {
            return dateTimeFormat.format((Date) messageAttribute);
        } else if (propertySpec.getName().equals(contactorModeAttributeName)) {
            return messageAttribute.toString();
        } else if (propertySpec.getName().equals(openKeyAttributeName) || propertySpec.getName().equals(transferKeyAttributeName)) {
            HexString hex = (HexString) messageAttribute;
            return hex.getContent();
        } else if (propertySpec.getName().equals(firmwareUpdateFileAttributeName)) {
            FirmwareVersion firmwareVersion = ((FirmwareVersion) messageAttribute);
            return GenericMessaging.zipAndB64EncodeContent(firmwareVersion.getFirmwareFile());  //Bytes of the firmwareFile as string
        }
        return EMPTY_FORMAT;
    }

    protected Map<DeviceMessageId, MessageEntryCreator> getRegistry() {
        Map<DeviceMessageId, MessageEntryCreator> registry = new HashMap<>();
        // contactor related
        registry.put(DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE, new DisconnectLoadWithActivationDateMessageEntry(contactorActivationDateAttributeName));
        registry.put(DeviceMessageId.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE, new ConnectLoadWithActivationDateMessageEntry(contactorActivationDateAttributeName));
        registry.put(DeviceMessageId.CONTACTOR_CHANGE_CONNECT_CONTROL_MODE, new ConnectControlModeMessageEntry(contactorModeAttributeName));

        // mbus related
        registry.put(DeviceMessageId.MBUS_SETUP_DECOMMISSION, new OneTagMessageEntry(RtuMessageConstant.MBUS_DECOMMISSION));
        registry.put(DeviceMessageId.MBUS_SETUP_SET_ENCRYPTION_KEYS, new SetMBusEncryptionKeysMessageEntry(openKeyAttributeName, transferKeyAttributeName));

        registry.put(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_LATER, new FirmwareUdateWithUserFileMessageEntry(firmwareUpdateFileAttributeName));
        return registry;
    }

}