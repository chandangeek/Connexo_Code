package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdw.core.UserFile;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
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
     * Represents a mapping between {@link DeviceMessageSpec deviceMessageSpecs}
     * and the corresponding {@link MessageEntryCreator}
     */
    private static Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>();

    static {
        registry.put(ConfigurationChangeDeviceMessage.UploadSwitchPointClockSettings, new ABBA1350UserFileMessageEntry(UploadSwitchPointClock));
        registry.put(ConfigurationChangeDeviceMessage.UploadSwitchPointClockUpdateSettings, new ABBA1350UserFileMessageEntry(UploadSwitchPointClockUpdate));
    }

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

    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return registry;
    }
}
