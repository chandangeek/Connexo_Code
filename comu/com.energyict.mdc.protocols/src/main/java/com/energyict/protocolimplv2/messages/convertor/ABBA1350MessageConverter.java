package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.protocol.api.UserFile;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.impl.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ABBA1350UserFileMessageEntry;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a MessageConverter for the legacy ABBA1350 IEC1107 protocol.
 *
 * @author sva
 * @since 25/10/13 - 9:35
 */
public class ABBA1350MessageConverter extends AbstractMessageConverter {

    private static final String CHARSET = "UTF-8";

    private static final String UploadSwitchPointClock = "SPC_DATA";
    private static final String UploadSwitchPointClockUpdate = "SPCU_DATA";

    /**
     * Default constructor for at-runtime instantiation
     */
    public ABBA1350MessageConverter() {
        super();
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(DeviceMessageConstants.SwitchPointClockSettings) || propertySpec.getName().equals(DeviceMessageConstants.SwitchPointClockUpdateSettings)) {
            return new String(((UserFile) messageAttribute).loadFileInByteArray(), Charset.forName(CHARSET));   // Content should be valid ASCII data
        } else {
            return messageAttribute.toString();
        }
    }

    protected Map<DeviceMessageId, MessageEntryCreator> getRegistry() {
        Map<DeviceMessageId, MessageEntryCreator> registry = new HashMap<>();
        registry.put(DeviceMessageId.CONFIGURATION_CHANGE_UPLOAD_SWITCH_POINT_CLOCK_SETTINGS, new ABBA1350UserFileMessageEntry(UploadSwitchPointClock));
        registry.put(DeviceMessageId.CONFIGURATION_CHANGE_UPLOAD_SWITCH_POINT_CLOCK_UPDATE_SETTINGS, new ABBA1350UserFileMessageEntry(UploadSwitchPointClockUpdate));
        return registry;
    }

}