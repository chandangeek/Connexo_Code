package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cbo.Password;
import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.SetMBusEncryptionKeysMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagMessageEntry;

import java.util.HashMap;
import java.util.Map;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.openKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.transferKeyAttributeName;

/**
 * Represents a MessageConverter for the DLMS AS220 GasDevice protocol
 * <p/>
 * Copyrights EnergyICT
 * Date: 8/03/13
 * Time: 16:26
 */
public class GasDeviceDLMSMessageConverter extends AbstractMessageConverter {

    /**
     * Represents a mapping between {@link com.energyict.mdc.messages.DeviceMessageSpec deviceMessageSpecs}
     * and the corresponding {@link MessageEntryCreator}
     */
    private static Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>();

    static {
        registry.put(ContactorDeviceMessage.CONTACTOR_OPEN, new SimpleTagMessageEntry("DisconnectGmeter"));
        registry.put(ContactorDeviceMessage.CONTACTOR_CLOSE, new SimpleTagMessageEntry("ConnectGmeter"));
        registry.put(MBusSetupDeviceMessage.Decommission, new SimpleTagMessageEntry("Decommission"));
        registry.put(MBusSetupDeviceMessage.SetEncryptionKeys, new SetMBusEncryptionKeysMessageEntry(openKeyAttributeName, transferKeyAttributeName, "EnableEncryption"));
        registry.put(MBusSetupDeviceMessage.WriteCaptureDefinition, new MultipleAttributeMessageEntry("WriteCaptureDefinition", "DIB", "VIB"));
    }

    /**
     * Default constructor for at-runtime instantiation
     */
    public GasDeviceDLMSMessageConverter() {
        super();
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(openKeyAttributeName) || propertySpec.getName().equals(transferKeyAttributeName)) {
            return ((Password) messageAttribute).getValue();
        }

        return messageAttribute.toString();//Works for hex, string and bigdecimal
    }

    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return registry;
    }
}