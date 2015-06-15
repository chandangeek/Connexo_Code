package com.energyict.protocolimplv2.messages.convertor;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.protocolimpl.generic.messages.GenericMessaging;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ABBA230UserFileMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagMessageEntry;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a MessageConverter for the legacy ABBA230 IEC1107 protocol.
 * <p/>
 * Copyrights EnergyICT
 * Date: 8/03/13
 * Time: 16:26
 */
public class ABBA230MessageConverter extends AbstractMessageConverter {

    private static final String CHARSET = "UTF-8";

    private static final String CONNECT_LOAD = "ConnectLoad";
    private static final String DISCONNECT_LOAD = "DisconnectLoad";
    private static final String ARM_METER = "ArmMeter";
    private static final String BILLING_RESET = "BillingReset";
    private static final String UPGRADE_METER_FIRMWARE = "UpgradeMeterFirmware";
    private static final String UPGRADE_METER_SCHEME = "UpgradeMeterScheme";

    /**
     * Default constructor for at-runtime instantiation
     */
    public ABBA230MessageConverter() {
        super();
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(DeviceMessageConstants.firmwareUpdateFileAttributeName) || propertySpec.getName().equals(DeviceMessageConstants.MeterScheme)) {
            FirmwareVersion firmwareVersion = ((FirmwareVersion) messageAttribute);
            return GenericMessaging.zipAndB64EncodeContent(firmwareVersion.getFirmwareFile());  //Bytes of the firmwareFile as string
        } else {
            return messageAttribute.toString();
        }
    }

    protected Map<DeviceMessageId, MessageEntryCreator> getRegistry() {
        Map<DeviceMessageId, MessageEntryCreator> registry = new HashMap<>();
        registry.put(DeviceMessageId.CONTACTOR_OPEN, new SimpleTagMessageEntry(CONNECT_LOAD, false));
        registry.put(DeviceMessageId.CONTACTOR_CLOSE, new SimpleTagMessageEntry(DISCONNECT_LOAD, false));
        registry.put(DeviceMessageId.CONTACTOR_ARM, new SimpleTagMessageEntry(ARM_METER, false));

        registry.put(DeviceMessageId.DEVICE_ACTIONS_DEMAND_RESET, new SimpleTagMessageEntry(BILLING_RESET, false));

        registry.put(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE, new ABBA230UserFileMessageEntry(UPGRADE_METER_FIRMWARE));

        registry.put(DeviceMessageId.CONFIGURATION_CHANGE_UPLOAD_METER_SCHEME, new ABBA230UserFileMessageEntry(UPGRADE_METER_SCHEME));
        return registry;
    }

}