/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.messages.convertor;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.DeviceMessageFile;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.protocols.messaging.DeviceMessageFileStringContentConsumer;

import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ABBA230UserFileMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagMessageEntry;

import java.util.HashMap;
import java.util.Map;

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
        if (propertySpec.getName().equals(DeviceMessageConstants.firmwareUpdateFileAttributeName)) {
            return messageAttribute.toString();     //This is the path of the temp file representing the FirmwareVersion
        } else if (propertySpec.getName().equals(DeviceMessageConstants.MeterScheme)) {
            return DeviceMessageFileStringContentConsumer.readFrom((DeviceMessageFile) messageAttribute, CHARSET);  //Return the content of the file, should be ASCII (XML)
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

        registry.put(DeviceMessageId.LOAD_BALANCING_DISABLE_LOAD_LIMITING, new MultipleAttributeMessageEntry("DISABLE_LOAD_LIMITING"));
        registry.put(DeviceMessageId.LOAD_BALANCING_SET_LOAD_LIMIT_DURATION, new MultipleAttributeMessageEntry("SET_LOAD_LIMIT_DURATION", "Duration"));
        registry.put(DeviceMessageId.LOAD_BALANCING_SET_LOAD_LIMIT_THRESHOLD, new MultipleAttributeMessageEntry("SET_LOAD_LIMIT_TRESHOLD", "Threshold", "Unit"));
        registry.put(DeviceMessageId.LOAD_BALANCING_CONFIGURE_LOAD_LIMIT_THRESHOLD_AND_DURATION, new MultipleAttributeMessageEntry("CONFIGURE_LOAD_LIMIT", "Threshold", "Unit", "Duration"));

        return registry;
    }
}