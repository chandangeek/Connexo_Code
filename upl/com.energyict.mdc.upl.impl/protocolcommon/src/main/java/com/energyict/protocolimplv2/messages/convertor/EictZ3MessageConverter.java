package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cbo.HexString;
import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.*;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.OneTagMessageEntry;

import java.util.*;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;

/**
 * Represents a MessageConverter for the legacy EictZ3 protocol
 * <p/>
 * Copyrights EnergyICT
 * Date: 8/03/13
 * Time: 16:26
 */
public class EictZ3MessageConverter extends AbstractMessageConverter {


    /**
     * Represents a mapping between {@link com.energyict.mdc.messages.DeviceMessageSpec deviceMessageSpecs}
     * and the corresponding {@link MessageEntryCreator}
     */
    private static Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>();

    static {
        // contactor related
        registry.put(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE, new DisconnectLoadWithActivationDateMessageEntry(contactorActivationDateAttributeName));
        registry.put(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE, new ConnectLoadWithActivationDateMessageEntry(contactorActivationDateAttributeName));
        registry.put(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE, new ConnectControlModeMessageEntry(contactorModeAttributeName));

        // display related
        registry.put(MBusSetupDeviceMessage.Decommission, new OneTagMessageEntry(RtuMessageConstant.MBUS_DECOMMISSION));
        registry.put(MBusSetupDeviceMessage.SetEncryptionKeys, new SetMBusEncryptionKeysMessageEntry(openKeyAttributeName, transferKeyAttributeName));
    }

    /**
     * Default constructor for at-runtime instantiation
     */
    public EictZ3MessageConverter() {
        super();
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(contactorActivationDateAttributeName)) {
            return dateFormat.format((Date) messageAttribute);
        } else if (propertySpec.getName().equals(contactorModeAttributeName)) {
            return messageAttribute.toString();
        } else if (propertySpec.getName().equals(openKeyAttributeName) || propertySpec.getName().equals(transferKeyAttributeName)) {
            HexString hex = (HexString) messageAttribute;
            return hex.getContent();
        }
        return EMPTY_FORMAT;
    }

    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return registry;
    }
}