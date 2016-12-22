package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ABBA230UserFileMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagMessageEntry;
import com.google.common.collect.ImmutableMap;

import java.nio.charset.Charset;
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

    public ABBA230MessageConverter(Messaging messagingProtocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        super(messagingProtocol, propertySpecService, nlsService, converter);
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(DeviceMessageConstants.firmwareUpdateUserFileAttributeName) || propertySpec.getName().equals(DeviceMessageConstants.MeterScheme)) {
            return new String(((DeviceMessageFile) messageAttribute).loadFileInByteArray(), Charset.forName(CHARSET)); // Content should be a valid XML
        } else {
            return messageAttribute.toString();
        }
    }

    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return ImmutableMap
                .<DeviceMessageSpec, MessageEntryCreator>builder()
                .put(messageSpec(ContactorDeviceMessage.CONTACTOR_OPEN), new SimpleTagMessageEntry(CONNECT_LOAD, false))
                .put(messageSpec(ContactorDeviceMessage.CONTACTOR_CLOSE), new SimpleTagMessageEntry(DISCONNECT_LOAD, false))
                .put(messageSpec(ContactorDeviceMessage.CONTACTOR_ARM), new SimpleTagMessageEntry(ARM_METER, false))

                .put(messageSpec(DeviceActionMessage.DEMAND_RESET), new SimpleTagMessageEntry(BILLING_RESET, false))
                .put(messageSpec(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE), new ABBA230UserFileMessageEntry(UPGRADE_METER_FIRMWARE))
                .put(messageSpec(ConfigurationChangeDeviceMessage.UploadMeterScheme), new ABBA230UserFileMessageEntry(UPGRADE_METER_SCHEME))
                .build();
    }
}
