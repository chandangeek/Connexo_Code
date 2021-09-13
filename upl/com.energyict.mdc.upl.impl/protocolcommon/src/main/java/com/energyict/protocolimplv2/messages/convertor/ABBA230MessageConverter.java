package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadBalanceDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ABBA230UserFileMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagMessageEntry;
import com.google.common.collect.ImmutableMap;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * Represents a MessageConverter for the legacy ABBA230 IEC1107 protocol.
 * <p>
 * Copyrights EnergyICT
 * Date: 8/03/13
 * Time: 16:26
 */
public class ABBA230MessageConverter extends AbstractMessageConverter {

    private static final String CHARSET = "UTF-8";

    private static final String CONNECT_LOAD = "ConnectLoad";
    private static final String DISCONNECT_LOAD = "DisconnectLoad";
    private static final String ARM_METER = "ArmMeter";
    private static final String UPGRADE_METER_FIRMWARE = "UpgradeMeterFirmware";
    private static final String UPGRADE_METER_SCHEME = "UploadMeterScheme";

    private final DeviceMessageFileExtractor deviceMessageFileExtractor;

    public ABBA230MessageConverter(PropertySpecService propertySpecService, NlsService nlsService, Converter converter, DeviceMessageFileExtractor deviceMessageFileExtractor) {
        super(propertySpecService, nlsService, converter);
        this.deviceMessageFileExtractor = deviceMessageFileExtractor;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(DeviceMessageConstants.firmwareUpdateFileAttributeName)) {
            return messageAttribute.toString();     //This is the path of the temp file representing the FirmwareVersion
        } else if (propertySpec.getName().equals(DeviceMessageConstants.MeterScheme)) {
            return this.deviceMessageFileExtractor.contents((DeviceMessageFile) messageAttribute, Charset.forName(CHARSET));    //Return the content of the file, should be ASCII (XML)
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

                .put(messageSpec(DeviceActionMessage.DEMAND_RESET), new SimpleTagMessageEntry(RtuMessageConstant.DEMAND_RESET, false))
                .put(messageSpec(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE), new ABBA230UserFileMessageEntry(UPGRADE_METER_FIRMWARE))
                .put(messageSpec(ConfigurationChangeDeviceMessage.UploadMeterScheme), new ABBA230UserFileMessageEntry(UPGRADE_METER_SCHEME))

                .put(messageSpec(LoadBalanceDeviceMessage.DISABLE_LOAD_LIMITING), new MultipleAttributeMessageEntry("DISABLE_LOAD_LIMITING"))
                .put(messageSpec(LoadBalanceDeviceMessage.SET_LOAD_LIMIT_DURATION), new MultipleAttributeMessageEntry("SET_LOAD_LIMIT_DURATION", "Duration"))
                .put(messageSpec(LoadBalanceDeviceMessage.SET_LOAD_LIMIT_THRESHOLD), new MultipleAttributeMessageEntry("SET_LOAD_LIMIT_TRESHOLD", "Threshold", "Unit"))
                .put(messageSpec(LoadBalanceDeviceMessage.CONFIGURE_LOAD_LIMIT_THRESHOLD_AND_DURATION), new MultipleAttributeMessageEntry("CONFIGURE_LOAD_LIMIT", "Threshold", "Unit", "Duration"))

                .build();
    }
}
